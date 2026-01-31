package com.launcher.aynthords

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity

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
    }

    private fun showLaunchMenu(label: String, intent: Intent) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Launch $label on…")
            .setItems(arrayOf("Top (0)", "Bottom (4)")) { _, which ->
                val displayId = if (which == 0)
                    DisplayAppLauncher.DISPLAY_TOP
                else
                    DisplayAppLauncher.DISPLAY_BOTTOM

                DisplayAppLauncher.launchOnDisplay(this, intent, displayId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Optional: log or ignore. This is basically “HOME was pressed again”.
    }

    override fun onResume() {
        super.onResume()
        Log.d("AynThor", "SecondaryHomeActivity RESUMED on displayId=${display?.displayId}")
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            android.util.Log.d("Keys", "display=${display?.displayId} code=${event.keyCode} name=${KeyEvent.keyCodeToString(event.keyCode)}")
            if (event.keyCode == KeyEvent.KEYCODE_BUTTON_X) {
                swapScreens()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun swapScreens() {
        // TODO: Implement screen swapping logic
        android.util.Log.d("Keys", "swapScreens() called")
    }
}