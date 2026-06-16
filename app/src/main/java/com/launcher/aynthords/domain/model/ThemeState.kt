package com.launcher.aynthords.domain.model

data class ThemeState(
    val activeThemeId: String,
    val themeSpec: Any, // Replace Any with your ThemeSpec model
    val themePack: Any? = null, // Replace Any with your ThemePack model
)
