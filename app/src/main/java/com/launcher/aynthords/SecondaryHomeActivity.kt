package com.launcher.aynthords

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.launcher.aynthords.display.ChangeSource
import com.launcher.aynthords.display.DisplayRoleStore
import com.launcher.aynthords.display.SurfaceRole
import kotlinx.coroutines.launch

class SecondaryHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary_home)

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            showLaunchMenu("Settings", DisplayAppLauncher.intentForSettings())
        }

        findViewById<View>(R.id.btnYouTube).setOnClickListener {
            showLaunchMenu("YouTube", DisplayAppLauncher.intentForYouTube(this))
        }

        DisplayRoleStore.reportDisplayValidation(display?.displayId, ChangeSource.STARTUP)
        observeRoleState()
    }

    override fun onResume() {
        super.onResume()
        DisplayRoleStore.reportDisplayValidation(display?.displayId, ChangeSource.RECOVERY)
        Log.d("AynThor", "SecondaryHomeActivity RESUMED on displayId=${display?.displayId}")
    }

    private fun observeRoleState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                DisplayRoleStore.state.collect { state ->
                    val role = DisplayRoleStore.roleForDisplayId(display?.displayId)
                    renderRoleState(role, state.validation.top.name, state.validation.bottom.name, state.sourceOfChange.name)
                }
            }
        }
    }

    private fun renderRoleState(role: SurfaceRole?, topValidation: String, bottomValidation: String, source: String) {
        val root = findViewById<View>(R.id.root)
        val txtRole = findViewById<TextView>(R.id.txtRole)
        val txtValidation = findViewById<TextView>(R.id.txtValidation)

        when (role) {
            SurfaceRole.INTERACTION -> {
                root.setBackgroundColor(Color.parseColor("#1A237E"))
                txtRole.setTextColor(Color.WHITE)
                txtValidation.setTextColor(Color.WHITE)
            }

            SurfaceRole.PRESENTATION -> {
                root.setBackgroundColor(Color.parseColor("#006064"))
                txtRole.setTextColor(Color.WHITE)
                txtValidation.setTextColor(Color.WHITE)
            }

            null -> {
                root.setBackgroundColor(Color.DKGRAY)
                txtRole.setTextColor(Color.WHITE)
                txtValidation.setTextColor(Color.WHITE)
            }
        }

        txtRole.text = "Role: ${role ?: "UNKNOWN"}"
        txtValidation.text = "source=$source, top=$topValidation, bottom=$bottomValidation"
    }

    private fun showLaunchMenu(label: String, intent: Intent) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Launch $label on…")
            .setItems(arrayOf("Interaction", "Presentation")) { _, which ->
                val role = if (which == 0) DisplayRole.INTERACTION else DisplayRole.PRESENTATION
                DisplayAppLauncher.launchOnRole(this, intent, role)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            android.util.Log.d("Keys", "display=${display?.displayId} code=${event.keyCode} name=${KeyEvent.keyCodeToString(event.keyCode)}")
            if (event.keyCode == KeyEvent.KEYCODE_BUTTON_X) {
                swapDisplayRoleMappings()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun swapScreens() {
        DisplayRoleStore.swapScreens(
            source = ChangeSource.USER_SWAP,
            displayId = display?.displayId,
        )
    }
}
