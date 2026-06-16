package com.launcher.aynthords

import android.os.Bundle
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.feature.launcher.Direction
import com.launcher.aynthords.feature.launcher.LauncherIntent
import com.launcher.aynthords.feature.launcher.LauncherStore
import com.launcher.aynthords.feature.launcher.ThorRoot
import com.launcher.aynthords.shell.display.DualScreenSessionController
import com.launcher.aynthords.shell.display.DisplayRegistry
import com.launcher.aynthords.shell.display.DisplayRoleMappingStore
import com.launcher.aynthords.shell.display.DisplayRoleStore
import com.launcher.aynthords.shell.display.ensureSecondaryDisplayActivity
import com.launcher.aynthords.shell.display.performUserSwap
import com.launcher.aynthords.shell.display.DisplaySessionController
import com.launcher.aynthords.shell.display.refreshEnsureDebounce
import com.launcher.aynthords.shell.display.recordOnPause
import com.launcher.aynthords.shell.display.likelyReturnedFromRecents
import com.launcher.aynthords.shell.HomeRoleHelper
import com.launcher.aynthords.DebugLog
import com.launcher.aynthords.input.Action
import com.launcher.aynthords.input.ActionDispatcher
import com.launcher.aynthords.input.NavDirection

class InteractionHostActivity : ComponentActivity() {
    private val displaySessionTag = "DisplaySession"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        LauncherStore.initialize(applicationContext)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No-op: consume back like a stock launcher
            }
        })

        val displayId = ContextCompat.getDisplayOrDefault(this).displayId

        setContent {
            val roleState by DisplayRoleStore.state.collectAsState()
            val surfaceRole = DisplayRoleStore.surfaceRoleFromState(roleState, displayId) ?: SurfaceRole.INTERACTION
            ThorRoot(
                surfaceRole = surfaceRole,
                displayId = displayId,
                onIntent = { intent -> LauncherStore.dispatch(intent, this@InteractionHostActivity) },
                onReassert = {
                    val result = DualScreenSessionController(this@InteractionHostActivity).reassert()
                    val message = when (result.status) {
                        DualScreenSessionController.Status.HEALTHY -> "Displays OK"
                        DualScreenSessionController.Status.RECOVERED -> "Recovery: ${result.message}"
                        DualScreenSessionController.Status.ERROR -> "Error: ${result.message}"
                    }
                    Toast.makeText(this@InteractionHostActivity, message, Toast.LENGTH_SHORT).show()
                },
                onSetAsDefaultLauncher = { HomeRoleHelper.launchRequestHomeRole(this@InteractionHostActivity) }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        recordOnPause()
    }

    override fun onResume() {
        super.onResume()
        if (likelyReturnedFromRecents()) {
            refreshEnsureDebounce()
        }
        val currentDisplayId = ContextCompat.getDisplayOrDefault(this).displayId

        // #region agent log
        DebugLog.log(applicationContext, "H5", "InteractionHostActivity.onResume", "onResume fired",
            "currentDisplayId" to currentDisplayId)
        // #endregion

        // Sync role mapping to persistent store when we have 2+ displays so reassert sees 0/4.
        val registry = DisplayRegistry(applicationContext)
        if (registry.snapshot().allDisplayIds.size >= 2) {
            val mapping = DisplayRoleStore.state.value.currentMapping
            val mappingStore = DisplayRoleMappingStore(this)
            mappingStore.setDisplayId(SurfaceRole.INTERACTION, DisplayRoleStore.displayIdForRole(mapping, SurfaceRole.INTERACTION))
            mappingStore.setDisplayId(SurfaceRole.PRESENTATION, DisplayRoleStore.displayIdForRole(mapping, SurfaceRole.PRESENTATION))
        }

        // Recompute session state and run recovery (launch second host if missing); no toast for recovery.
        val sessionController = DisplaySessionController(this)
        val sessionResult = sessionController.reassert(currentDisplayId)

        Log.i(
            displaySessionTag,
            "status=${sessionResult.status} " +
                "roleMap=${sessionResult.state.roleMap} " +
                "available=${sessionResult.state.availableDisplayIds} " +
                "actions=${sessionResult.actions}"
        )

        val result = DualScreenSessionController(this).reassert()
        when (result.status) {
            DualScreenSessionController.Status.RECOVERED -> { /* silent: second screen launched */ }
            DualScreenSessionController.Status.ERROR ->
                Toast.makeText(this, "Display: ${result.message}", Toast.LENGTH_LONG).show()
            DualScreenSessionController.Status.HEALTHY -> {
                refreshEnsureDebounce()
            }
        }
    }

    /**
     * Intercept global keys before they reach the view hierarchy (e.g. game grid).
     * This ensures Start/Menu/X work regardless of which view has focus.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            // When settings open, intercept DPAD/Confirm FIRST so game grid never sees them
            if (LauncherStore.state.value.settingsOpen) {
                val nav = ActionDispatcher.resolveNav(event.keyCode)
                val action = ActionDispatcher.resolveAction(event.keyCode)
                if (nav != null || action == Action.Confirm) {
                    if (com.launcher.aynthords.feature.settings.processSettingsKeyEvent(this, event.keyCode)) {
                        return true
                    }
                }
            }

            // Rebinding: capture any key (B, Start, etc.) before it closes settings
            if (LauncherStore.state.value.settingsOpen &&
                com.launcher.aynthords.feature.settings.SettingsStore.state.value.selectedSubpageId == "customizeControls" &&
                com.launcher.aynthords.input.InputConfig.rebindingAction.value != null
            ) {
                if (com.launcher.aynthords.input.InputConfig.captureRebindingKey(this, event.keyCode)) {
                    return true
                }
            }

            val action = ActionDispatcher.resolveAction(event.keyCode)
            when (action) {
                Action.QuickSwapDisplays -> {
                    val displayId = ContextCompat.getDisplayOrDefault(this).displayId
                    DebugLog.log(this, "H1", "InteractionHostActivity.dispatchKeyEvent", "QuickSwap", "displayId" to displayId)
                    performUserSwap(this, displayId)
                    return true
                }
                Action.Menu -> {
                    val state = LauncherStore.state.value
                    if (state.settingsOpen) {
                        LauncherStore.dispatch(LauncherIntent.CloseSettings)
                    } else {
                        if (DisplayRegistry(applicationContext).snapshot().allDisplayIds.size >= 2) {
                            ensureSecondaryDisplayActivity()
                        }
                        LauncherStore.dispatch(LauncherIntent.OpenSettings)
                    }
                    return true
                }
                Action.Back -> {
                    if (LauncherStore.state.value.settingsOpen) {
                        if (com.launcher.aynthords.feature.settings.SettingsStore.handleBack()) {
                            return true
                        }
                        LauncherStore.dispatch(LauncherIntent.CloseSettings)
                    }
                    return true
                }
                Action.ResetCategory -> {
                    if (LauncherStore.state.value.settingsOpen) {
                        com.launcher.aynthords.feature.settings.SettingsStore.resetCategory()
                        return true
                    }
                }
                else -> {}
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val state = LauncherStore.state.value
        val action = ActionDispatcher.resolveAction(keyCode)
        val nav = ActionDispatcher.resolveNav(keyCode)

        when {
            nav != null -> {
                val direction = when (nav) {
                    NavDirection.Up -> Direction.Up
                    NavDirection.Down -> Direction.Down
                    NavDirection.Left -> Direction.Left
                    NavDirection.Right -> Direction.Right
                }
                LauncherStore.dispatch(LauncherIntent.MoveFocus(direction))
                return true
            }
            action == Action.Back -> {
                if (state.detailsEntryId != null) {
                    LauncherStore.dispatch(LauncherIntent.CloseDetails)
                    return true
                }
                if (state.launchError != null) {
                    LauncherStore.dispatch(LauncherIntent.DismissLaunchError)
                    return true
                }
            }
            action == Action.Details -> {
                state.focusedEntryId?.let { id ->
                    LauncherStore.dispatch(LauncherIntent.OpenDetails(id))
                }
                return true
            }
            action == Action.Confirm -> {
                LauncherStore.dispatch(LauncherIntent.ConfirmSelection)
                LauncherStore.dispatch(LauncherIntent.LaunchSelected, this)
                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }
}
