package com.launcher.aynthords

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

enum class HomeRole(
    val title: String,
    val subtitle: String,
) {
    INTERACTION_SHELL(
        title = "Interaction Shell",
        subtitle = "Default-display home surface for user interaction.",
    ),
    PRESENTATION_SHELL(
        title = "Presentation Shell",
        subtitle = "Secondary-display home surface for presentation.",
    ),
}

@Composable
fun RoleHomeSurface(
    activity: Activity,
    role: HomeRole,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    var pendingIntent by remember { mutableStateOf<Intent?>(null) }
    var pendingLabel by remember { mutableStateOf("") }

    fun promptLaunch(label: String, intent: Intent) {
        pendingLabel = label
        pendingIntent = intent
        showDialog = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = role.title, style = MaterialTheme.typography.headlineMedium)
        Text(text = role.subtitle, style = MaterialTheme.typography.bodyLarge)

        Button(onClick = { promptLaunch("Settings", DisplayAppLauncher.intentForSettings()) }) {
            Text("Launch Settings")
        }

        Button(onClick = { promptLaunch("YouTube", DisplayAppLauncher.intentForYouTube(activity)) }) {
            Text("Launch YouTube")
        }
    }

    if (showDialog && pendingIntent != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Launch $pendingLabel on…") },
            text = { Text("Choose the display to launch on for HOME testing.") },
            confirmButton = {
                TextButton(onClick = {
                    DisplayAppLauncher.launchOnDisplay(
                        activity,
                        pendingIntent!!,
                        DisplayAppLauncher.DISPLAY_TOP,
                    )
                    showDialog = false
                }) {
                    Text("Top (0)")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        DisplayAppLauncher.launchOnDisplay(
                            activity,
                            pendingIntent!!,
                            DisplayAppLauncher.DISPLAY_BOTTOM,
                        )
                        showDialog = false
                    }) {
                        Text("Bottom (4)")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
        )
    }
}
