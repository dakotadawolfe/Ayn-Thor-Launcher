package com.launcher.aynthords.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import com.launcher.aynthords.theme.runtime.ThemeRuntime

/**
 * Builds a Material 3 [ColorScheme] from [ThemeRuntime] tokens so the whole UI agrees with the theme palette.
 */
fun ThemeRuntime.toMaterialColorScheme(): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onSurface,
    primaryContainer = surfaceVariant,
    onPrimaryContainer = onSurfaceVariant,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
)
