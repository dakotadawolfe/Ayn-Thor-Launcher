package com.launcher.aynthords

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.launcher.aynthords.theme.ThemeRepository
import com.launcher.aynthords.ui.theme.ThorLauncherTheme
import com.launcher.aynthords.ui.theme.ThorTheme
import com.launcher.aynthords.display.ChangeSource
import com.launcher.aynthords.display.DisplayRoleState
import com.launcher.aynthords.display.DisplayRoleStore
import com.launcher.aynthords.display.SurfaceRole

class PrimaryHomeActivity : ComponentActivity() {

    private val sessionController by lazy { DualScreenSessionController(this) }
    private val recoveryMessage = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureSecondaryDisplayActivity()
        DisplayRoleStore.reportDisplayValidation(display?.displayId, ChangeSource.STARTUP)

        val activeTheme = ThemeRepository.loadBestTheme(this)
        setContent {
            ThorLauncherTheme(themeSpec = activeTheme.spec) {
                LaunchTestPanel(activity = this)
            }
            val state by DisplayRoleStore.state.collectAsState()
            val role = DisplayRoleStore.roleForDisplayId(display?.displayId)
            LaunchTestPanel(activity = this, role = role, state = state)
        setContent {
            LaunchTestPanel(
                activity = this,
                recoveryMessage = recoveryMessage.value,
                onRetryRecovery = { reassertDualScreenSession() }
            )
        }

        reassertDualScreenSession()
    }

    override fun onResume() {
        super.onResume()
        reassertDualScreenSession()
    }

    override fun onResume() {
        super.onResume()
        DisplayRoleStore.reportDisplayValidation(display?.displayId, ChangeSource.RECOVERY)
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

    private fun reassertDualScreenSession() {
        val result = sessionController.reassert()
        recoveryMessage.value = when (result.status) {
            DualScreenSessionController.Status.HEALTHY -> null
            DualScreenSessionController.Status.RECOVERED,
            DualScreenSessionController.Status.ERROR -> result.message
        }
    }

    private fun swapScreens() {
        DisplayRoleStore.swapScreens(
            source = ChangeSource.USER_SWAP,
            displayId = display?.displayId,
        )
    }
}

@Composable
fun LaunchTestPanel(activity: android.app.Activity) {
    val interactionLayout = ThorTheme.layout.interaction
fun LaunchTestPanel(
    activity: android.app.Activity,
    role: SurfaceRole?,
    state: DisplayRoleState,
    recoveryMessage: String?,
    onRetryRecovery: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var pendingIntent by remember { mutableStateOf<android.content.Intent?>(null) }
    var pendingLabel by remember { mutableStateOf("") }

    fun promptLaunch(label: String, intent: android.content.Intent) {
        pendingLabel = label
        pendingIntent = intent
        showDialog = true
    }

    val backgroundColor = when (role) {
        SurfaceRole.INTERACTION -> Color(0xFF0D47A1)
        SurfaceRole.PRESENTATION -> Color(0xFF004D40)
        null -> Color.DarkGray
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(interactionLayout.paddingDp.dp),
        verticalArrangement = Arrangement.spacedBy(interactionLayout.spacingDp.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("PRIMARY HOME")
        Text("Role: ${role ?: "UNKNOWN"}")
        Text("Source: ${state.sourceOfChange}")
        Text("Validation top=${state.validation.top}, bottom=${state.validation.bottom}")
        if (recoveryMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dual-screen recovery required",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = recoveryMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(onClick = onRetryRecovery) {
                        Text("Retry secondary launch")
                    }
                }
            }
        }

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
            text = { Text("Choose the logical display role for HOME testing.") },
            title = { Text("Launch $pendingLabel on…") },
            text = { Text("Choose the display to launch on for HOME testing.") },
            confirmButton = {
                TextButton(onClick = {
                    DisplayAppLauncher.launchOnRole(
                        activity,
                        pendingIntent!!,
                        DisplayRole.INTERACTION
                        DisplayAppLauncher.DISPLAY_TOP,
                    )
                    showDialog = false
                }) { Text("Interaction") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        DisplayAppLauncher.launchOnRole(
                            activity,
                            pendingIntent!!,
                            DisplayRole.PRESENTATION
                            DisplayAppLauncher.DISPLAY_BOTTOM,
                        )
                        showDialog = false
                    }) { Text("Bottom (4)") }
                    Spacer(Modifier.width(interactionLayout.spacingDp.dp))
                    }) { Text("Presentation") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            },
        )
    }
}
