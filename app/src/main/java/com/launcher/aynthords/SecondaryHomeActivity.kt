package com.launcher.aynthords

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.launcher.aynthords.ui.theme.ThorLauncherTheme

class SecondaryHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThorLauncherTheme {
                RoleHomeSurface(
                    activity = this,
                    role = HomeRole.PRESENTATION_SHELL,
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
