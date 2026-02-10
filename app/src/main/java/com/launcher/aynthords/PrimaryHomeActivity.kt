package com.launcher.aynthords

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.launcher.aynthords.ui.theme.ThorLauncherTheme

class PrimaryHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // On any entry to "home", make sure we own the other screen too.
        ensureSecondaryDisplayActivity()

        setContent {
            ThorLauncherTheme {
                RoleHomeSurface(
                    activity = this,
                    role = HomeRole.INTERACTION_SHELL,
                )
            }
        }
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
