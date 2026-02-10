package com.launcher.aynthords.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.launcher.aynthords.theme.ThemeLayout
import com.launcher.aynthords.theme.ThemeMotion
import com.launcher.aynthords.theme.ThemeSpecDefaults
import com.launcher.aynthords.theme.ThemeSpecV1

val LocalThemeMotion = staticCompositionLocalOf<ThemeMotion> { ThemeSpecDefaults.spec.motion }
val LocalThemeLayout = staticCompositionLocalOf<ThemeLayout> { ThemeSpecDefaults.spec.layout }

object ThorTheme {
    val motion: ThemeMotion
        get() = LocalThemeMotion.current

    val layout: ThemeLayout
        get() = LocalThemeLayout.current
}

fun defaultRuntimeThemeSpec(): ThemeSpecV1 = ThemeSpecDefaults.spec
