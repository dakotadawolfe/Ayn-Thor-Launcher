package com.launcher.aynthords.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.launcher.aynthords.theme.runtime.LocalThemeRuntime
import com.launcher.aynthords.theme.runtime.ThemeRuntime
import com.launcher.aynthords.theme.spec.ThemeSpecV1
import com.launcher.aynthords.ui.theme.toMaterialColorScheme

/** Console-style structure colors: flat, high contrast, no gray fog. */
data class SettingsStructure(
    val baseBg: Color,
    val railBg: Color,
    val contentBg: Color,
    val accentColor: Color,
)

val LocalSettingsStructure = compositionLocalOf<SettingsStructure> {
    error("No SettingsStructure provided")
}

/**
 * Fixed theme for the Settings UI. Settings uses this instead of user themes
 * so the layout and components stay consistent regardless of theme selection.
 * Console aesthetic: flat, structured, high contrast.
 */
@Composable
fun SystemSettingsTheme(content: @Composable () -> Unit) {
    val tokens = ThemeRuntime.fromSpec(ThemeSpecV1(), densityMultiplier = 1f)
    val structure = SettingsStructure(
        baseBg = Color(0xFF0D0D0D),
        railBg = Color(0xFF111111),
        contentBg = Color(0xFF161616),
        accentColor = Color(0xFFFF6B00), // Thor orange for focus
    )
    MaterialTheme(colorScheme = tokens.toMaterialColorScheme()) {
        CompositionLocalProvider(
            LocalThemeRuntime provides tokens,
            LocalSettingsStructure provides structure,
        ) {
            content()
        }
    }
}
