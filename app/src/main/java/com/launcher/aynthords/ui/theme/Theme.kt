package com.launcher.aynthords.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.launcher.aynthords.theme.ThemeColorTokens
import com.launcher.aynthords.theme.ThemeSpecV1
import com.launcher.aynthords.theme.ThemeTextStyle

@Composable
fun ThorLauncherTheme(
    themeSpec: ThemeSpecV1,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        themeSpec.palette.dark.toColorScheme(isDark = true)
    } else {
        themeSpec.palette.light.toColorScheme(isDark = false)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = themeSpec.toTypography(),
    ) {
        CompositionLocalProvider(
            LocalThemeMotion provides themeSpec.motion,
            LocalThemeLayout provides themeSpec.layout,
            content = content
        )
    }
}

private fun ThemeSpecV1.toTypography(): Typography {
    return Typography(
        bodyLarge = typography.bodyLarge.toComposeTextStyle(),
        titleLarge = typography.titleLarge.toComposeTextStyle(),
        labelSmall = typography.labelSmall.toComposeTextStyle()
    )
}

private fun ThemeTextStyle.toComposeTextStyle(): TextStyle {
    return TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(fontWeight),
        fontSize = fontSizeSp.sp,
        lineHeight = lineHeightSp.sp,
        letterSpacing = letterSpacingSp.sp
    )
}

private fun ThemeColorTokens.toColorScheme(isDark: Boolean): ColorScheme {
    val primary = parseColor(primary)
    val onPrimary = parseColor(onPrimary)
    val secondary = parseColor(secondary)
    val onSecondary = parseColor(onSecondary)
    val tertiary = parseColor(tertiary)
    val onTertiary = parseColor(onTertiary)
    val background = parseColor(background)
    val onBackground = parseColor(onBackground)
    val surface = parseColor(surface)
    val onSurface = parseColor(onSurface)

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onTertiary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onTertiary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface
        )
    }
}

private fun parseColor(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}
