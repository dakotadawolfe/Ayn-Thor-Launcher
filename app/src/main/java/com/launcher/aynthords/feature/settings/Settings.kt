package com.launcher.aynthords.feature.settings

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.launcher.aynthords.data.export.ExportPayload
import com.launcher.aynthords.data.local.GameProfileStore
import com.launcher.aynthords.data.local.LibraryCurationStore
import com.launcher.aynthords.data.local.ProfileRepositoryImpl
import com.launcher.aynthords.shell.display.DualScreenSessionController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val json = Json { encodeDefaults = true }

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val profileRepo = remember { ProfileRepositoryImpl(GameProfileStore(context)) }
    val curationStore = remember { LibraryCurationStore(context) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val text = input.bufferedReader().readText()
                val payload = json.decodeFromString<ExportPayload>(text)
                if (payload.schemaVersion != 1) {
                    Toast.makeText(context, "Unsupported export format version", Toast.LENGTH_LONG).show()
                    return@rememberLauncherForActivityResult
                }
                scope.launch {
                    for (profile in payload.profiles) {
                        profileRepo.updateProfile(profile.id) { _ -> profile }
                    }
                    val colls = payload.collectionsAsSets()
                    for ((collId, pkgs) in colls) {
                        for (pkg in pkgs) {
                            curationStore.addToCollection(pkg, collId)
                        }
                    }
                    Toast.makeText(context, "Import complete", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(context, "Could not read file", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Reassert displays",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (activity != null) {
                            val result = DualScreenSessionController(activity).reassert()
                            val message = when (result.status) {
                                DualScreenSessionController.Status.HEALTHY -> "Displays OK"
                                DualScreenSessionController.Status.RECOVERED -> "Recovery: ${result.message}"
                                DualScreenSessionController.Status.ERROR -> "Error: ${result.message}"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                    .padding(16.dp)
            )
            Text(
                text = "Export profiles",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            try {
                                val profileMap = profileRepo.observeProfiles().first()
                                val curation = curationStore.curation.first()
                                val colls = curation.collections.mapValues { (_, v) -> v.toList() }
                                val payload = ExportPayload(
                                    profiles = profileMap.values.toList(),
                                    collections = colls,
                                )
                                val jsonStr = json.encodeToString(payload)
                                val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                val fileName = "aynthor_profiles_$date.json"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_TEXT, jsonStr)
                                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                                }
                                activity?.startActivity(Intent.createChooser(intent, "Export profiles"))
                                Toast.makeText(context, "Export ready", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .padding(16.dp)
            )
            Text(
                text = "Import profiles",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    }
                    .padding(16.dp)
            )
        }
    }
}
