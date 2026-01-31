package com.launcher.aynthords

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class PrimaryHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // On any entry to "home", make sure we own the other screen too.
        ensureSecondaryDisplayActivity()

        setContent {
            LaunchTestPanel(activity = this)
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

@Composable
fun LaunchTestPanel(activity: android.app.Activity) {
    var showDialog by remember { mutableStateOf(false) }
    var pendingIntent by remember { mutableStateOf<android.content.Intent?>(null) }
    var pendingLabel by remember { mutableStateOf("") }

    fun promptLaunch(label: String, intent: android.content.Intent) {
        pendingLabel = label
        pendingIntent = intent
        showDialog = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            promptLaunch("Settings", DisplayAppLauncher.intentForSettings())
        }) { Text("Launch Settings") }

        Button(onClick = {
            promptLaunch("YouTube", DisplayAppLauncher.intentForYouTube(activity))
        }) { Text("Launch YouTube") }
    }

    if (showDialog && pendingIntent != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Launch ${pendingLabel} on…") },
            text = { Text("Choose the display to launch on for HOME testing.") },
            confirmButton = {
                TextButton(onClick = {
                    DisplayAppLauncher.launchOnDisplay(
                        activity,
                        pendingIntent!!,
                        DisplayAppLauncher.DISPLAY_TOP
                    )
                    showDialog = false
                }) { Text("Top (0)") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        DisplayAppLauncher.launchOnDisplay(
                            activity,
                            pendingIntent!!,
                            DisplayAppLauncher.DISPLAY_BOTTOM
                        )
                        showDialog = false
                    }) { Text("Bottom (4)") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            }
        )
    }
}
