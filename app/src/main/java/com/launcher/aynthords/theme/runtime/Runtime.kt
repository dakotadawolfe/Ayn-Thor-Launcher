package com.launcher.aynthords.theme.runtime

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.launcher.aynthords.theme.spec.ThemeSpecV1
import com.launcher.aynthords.theme.spec.ThemeSubsets

/**
 * Resolved theme tokens for use in composables. Built from [ThemeSpecV1].
 * [densityMultiplier] (e.g. 0.85f, 1f, 1.15f) scales spacing and size tokens for user density preference.
 */
data class ThemeRuntime(
    val primary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val spacing: Dp,
    val cornerRadius: Dp,
    val motionDurationMs: Int,
    val railWidth: Dp,
    val gridGap: Dp,
    val gridItemCornerRadius: Dp,
    val gridItemAspectRatio: Float,
    val focusedBorderWidth: Dp,
    val focusedScale: Float,
    val hintBarHeight: Dp,
    val metadataPanelSizeFraction: Float,
    val titleSizeSp: TextUnit,
    val bodySizeSp: TextUnit,
    val captionSizeSp: TextUnit,
    /** Resolved subset/variant keys (e.g. density, iconset, metadata, presentation) for theme-driven variants. */
    val subsets: Map<String, String>
) {
    companion object {
        fun fromSpec(spec: ThemeSpecV1, densityMultiplier: Float = 1f, subsetOverrides: Map<String, String>? = null): ThemeRuntime {
            val d = densityMultiplier.coerceIn(0.5f, 2f)
            val layout = spec.layout
            return ThemeRuntime(
                primary = parseHex(spec.palette.primary),
                surface = parseHex(spec.palette.surface),
                onSurface = parseHex(spec.palette.onSurface),
                surfaceVariant = parseHex(spec.palette.surfaceVariant),
                onSurfaceVariant = parseHex(spec.palette.onSurfaceVariant),
                spacing = (layout.spacing * d).toInt().dp,
                cornerRadius = (layout.cornerRadius * d).toInt().dp,
                motionDurationMs = spec.motion.duration,
                railWidth = (layout.railWidthDp * d).toInt().dp,
                gridGap = (layout.gridGapDp * d).toInt().dp,
                gridItemCornerRadius = (layout.gridItemCornerRadiusDp * d).toInt().dp,
                gridItemAspectRatio = layout.gridItemAspectRatio,
                focusedBorderWidth = (layout.focusedBorderWidthDp * d).toInt().dp,
                focusedScale = layout.focusedScale,
                hintBarHeight = (layout.hintBarHeightDp * d).toInt().dp,
                metadataPanelSizeFraction = layout.metadataPanelSizeFraction,
                titleSizeSp = (spec.typography.titleSizeSp * d).toInt().sp,
                bodySizeSp = (spec.typography.bodySizeSp * d).toInt().sp,
                captionSizeSp = (spec.typography.captionSizeSp * d).toInt().sp,
                subsets = resolveSubsets(spec.subsets, subsetOverrides)
            )
        }

        private fun resolveSubsets(defaults: ThemeSubsets, overrides: Map<String, String>?): Map<String, String> {
            val base = mapOf(
                "density" to defaults.density,
                "iconset" to defaults.iconset,
                "metadata" to defaults.metadata,
                "presentation" to defaults.presentation
            )
            return if (overrides.isNullOrEmpty()) base else base + overrides
        }
    }
}

fun parseHex(hex: String): Color {
    val s = hex.trim().removePrefix("#")
    val alpha = when (s.length) {
        6 -> 0xFF000000L
        8 -> 0L
        else -> 0xFF000000L
    }
    val value = s.toLongOrNull(16) ?: 0xFFFFFFFFL
    val colorValue = if (s.length == 6) (alpha or value).toLong() else value
    return Color(colorValue.toInt())
}

val LocalThemeRuntime = compositionLocalOf<ThemeRuntime> {
    error("No ThemeRuntime provided. Wrap content in a provider that loads theme spec and provides ThemeRuntime.")
}
