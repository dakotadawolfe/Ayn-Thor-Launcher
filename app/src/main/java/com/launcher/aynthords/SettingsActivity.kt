package com.launcher.aynthords

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.launcher.aynthords.feature.settings.SettingsScreen
import com.launcher.aynthords.theme.ThemeRepository
import com.launcher.aynthords.theme.runtime.LocalThemeRuntime
import com.launcher.aynthords.theme.runtime.ThemeRuntime
import com.launcher.aynthords.theme.spec.ThemeSpecV1
import com.launcher.aynthords.ui.theme.toMaterialColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeSpec by remember { mutableStateOf<ThemeSpecV1?>(null) }
            LaunchedEffect(Unit) {
                themeSpec = withContext(Dispatchers.IO) {
                    ThemeRepository.loadThemeSpec(applicationContext)
                }
            }
            val tokens = remember(themeSpec) {
                ThemeRuntime.fromSpec(themeSpec ?: ThemeSpecV1())
            }
            MaterialTheme(colorScheme = tokens.toMaterialColorScheme()) {
                CompositionLocalProvider(LocalThemeRuntime provides tokens) {
                    SettingsScreen()
                }
            }
        }
    }

    companion object {
        fun intent(activity: ComponentActivity): Intent =
            Intent(activity, SettingsActivity::class.java)
    }
}
