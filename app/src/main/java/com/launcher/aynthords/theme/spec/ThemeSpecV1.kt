package com.launcher.aynthords.theme.spec

import kotlinx.serialization.Serializable

@Serializable
data class ThemeSpecV1(
    val schemaVersion: Int = 1,
    val metadata: ThemeMetadata = ThemeMetadata(),
    val palette: ThemePalette = ThemePalette(),
    val typography: ThemeTypography = ThemeTypography(),
    val motion: ThemeMotion = ThemeMotion(),
    val layout: ThemeLayout = ThemeLayout(),
    /** Resolved at runtime; theme can swap variants (e.g. density: comfy|compact, metadata: minimal|rich). */
    val subsets: ThemeSubsets = ThemeSubsets()
)

@Serializable
data class ThemeSubsets(
    val density: String = "comfy",
    val iconset: String = "flat",
    val metadata: String = "rich",
    val presentation: String = "artPlusDetails"
)

@Serializable
data class ThemeMetadata(
    val name: String = "Default",
    val version: String = "1.0",
    val author: String = "Ayn",
    val compatibility: ThemeCompatibility = ThemeCompatibility()
)

@Serializable
data class ThemeCompatibility(
    val minSdk: Int = 31,
    val minAppVersion: String = "1.0"
)

@Serializable
data class ThemePalette(
    val primary: String = "#FFFFFFFF",
    val surface: String = "#FF1A1A1A",
    val onSurface: String = "#FFFFFFFF",
    val surfaceVariant: String = "#FF2A2A2A",
    val onSurfaceVariant: String = "#FFB0B0B0"
)

@Serializable
data class ThemeTypography(
    val default: String = "default",
    val titleSizeSp: Int = 20,
    val bodySizeSp: Int = 16,
    val captionSizeSp: Int = 12
)

@Serializable
data class ThemeMotion(val duration: Int = 300)

@Serializable
data class ThemeLayout(
    val spacing: Int = 16,
    val cornerRadius: Int = 8,
    val railWidthDp: Int = 120,
    val gridGapDp: Int = 12,
    val gridItemCornerRadiusDp: Int = 8,
    val gridItemAspectRatio: Float = 2f / 3f,
    val focusedBorderWidthDp: Int = 2,
    val focusedScale: Float = 1.02f,
    val hintBarHeightDp: Int = 48,
    val metadataPanelSizeFraction: Float = 0.4f
)
