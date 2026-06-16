package com.launcher.aynthords

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.feature.launcher.LauncherIntent
import com.launcher.aynthords.feature.launcher.LauncherStore
import com.launcher.aynthords.feature.launcher.ThorRoot
import com.launcher.aynthords.shell.display.DualScreenSessionController
import com.launcher.aynthords.shell.display.DisplayRoleStore
import com.launcher.aynthords.shell.display.DisplayRegistry
import com.launcher.aynthords.shell.display.ensureSecondaryDisplayActivity
import com.launcher.aynthords.shell.display.performUserSwap
import com.launcher.aynthords.shell.display.refreshEnsureDebounce
import com.launcher.aynthords.shell.display.recordOnPause
import com.launcher.aynthords.DebugLog
import com.launcher.aynthords.input.Action
import com.launcher.aynthords.input.ActionDispatcher

class PresentationHostActivity : ComponentActivity() {
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
            val surfaceRole = DisplayRoleStore.surfaceRoleFromState(roleState, displayId) ?: SurfaceRole.PRESENTATION
            ThorRoot(
                surfaceRole = surfaceRole,
                displayId = displayId,
                onIntent = { intent -> LauncherStore.dispatch(intent, this@PresentationHostActivity) },
                onReassert = {
                    val result = DualScreenSessionController(this@PresentationHostActivity).reassert()
                    val message = when (result.status) {
                        DualScreenSessionController.Status.HEALTHY -> "Displays OK"
                        DualScreenSessionController.Status.RECOVERED -> "Recovery: ${result.message}"
                        DualScreenSessionController.Status.ERROR -> "Error: ${result.message}"
                    }
                    Toast.makeText(this@PresentationHostActivity, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        recordOnPause()
    }

    override fun onResume() {
        super.onResume()
        refreshEnsureDebounce()
    }

    /**
     * Intercept global keys before they reach the view hierarchy.
     * Mirrors InteractionHostActivity so Start/Menu/X work the same on either display.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            // When settings open, intercept DPAD/Confirm FIRST so underlying views never see them
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
                    DebugLog.log(this, "H1", "PresentationHostActivity.dispatchKeyEvent", "QuickSwap", "displayId" to displayId)
                    performUserSwap(this, displayId)
                    return true
                }
                Action.Menu -> {
                    val settingsOpen = LauncherStore.state.value.settingsOpen
                    if (settingsOpen) {
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
}
