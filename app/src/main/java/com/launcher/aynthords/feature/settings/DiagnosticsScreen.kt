package com.launcher.aynthords.feature.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.launcher.aynthords.feature.launcher.LauncherStore
import com.launcher.aynthords.shell.display.DisplayRoleStore
import com.launcher.aynthords.theme.runtime.LocalThemeRuntime
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diagnostics panel for beta testers: export logs, display role state, selection ID.
 * Flat rows with dividers, no cards.
 */
@Composable
fun DiagnosticsContent(onDismiss: () -> Unit) {
    val theme = LocalThemeRuntime.current
    val context = LocalContext.current
    val launcherState by LauncherStore.state.collectAsState()
    val roleState by DisplayRoleStore.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Diagnostics",
            color = theme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        DiagnosticsRow(
            label = "Export logs",
            subtitle = "Share debug logs for bug reports",
            onClick = { exportLogs(context) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(theme.surfaceVariant.copy(alpha = 0.2f))
        )
        DiagnosticsInfoBlock(
            title = "Display role state",
            content = buildDisplayRoleStateText(roleState),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(theme.surfaceVariant.copy(alpha = 0.2f))
        )
        DiagnosticsInfoBlock(
            title = "Focused display / selection",
            content = buildSelectionStateText(launcherState),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(theme.surfaceVariant.copy(alpha = 0.2f))
        )
        DiagnosticsRow(
            label = "Back",
            subtitle = "Return to System settings",
            onClick = onDismiss,
        )
    }
}

@Composable
private fun DiagnosticsRow(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val theme = LocalThemeRuntime.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Column {
            Text(
                text = label,
                color = theme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                color = theme.onSurfaceVariant.copy(alpha = 0.65f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun DiagnosticsInfoBlock(
    title: String,
    content: String,
) {
    val theme = LocalThemeRuntime.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = title,
            color = theme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Text(
            text = content,
            color = theme.onSurfaceVariant.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private fun buildDisplayRoleStateText(roleState: com.launcher.aynthords.shell.display.DisplayRoleState): String {
    val mapping = roleState.currentMapping
    val validation = roleState.validation
    val rejected = roleState as? com.launcher.aynthords.shell.display.DisplayRoleState.Rejected
    return buildString {
        appendLine("top=${mapping.top}, bottom=${mapping.bottom}")
        appendLine("validation: top=${validation.top}, bottom=${validation.bottom}")
        if (rejected != null) {
            appendLine("rejected: ${rejected.reason}")
        }
    }
}

private fun buildSelectionStateText(launcherState: com.launcher.aynthords.feature.launcher.LauncherUiState): String {
    return buildString {
        appendLine("focusedEntryId=${launcherState.focusedEntryId}")
        appendLine("selectedEntryId=${launcherState.selectedEntryId}")
        appendLine("entriesCount=${launcherState.entries.size}")
    }
}

private fun exportLogs(context: Context) {
    try {
        val debugFile = File(context.filesDir, "debug_agent.ndjson")
        if (!debugFile.exists()) {
            Toast.makeText(context, "No logs to export yet", Toast.LENGTH_SHORT).show()
            return
        }
        val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val exportFile = File(context.cacheDir, "aynthor_logs_$date.ndjson")
        debugFile.copyTo(exportFile, overwrite = true)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export logs"))
        Toast.makeText(context, "Logs ready to share", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
