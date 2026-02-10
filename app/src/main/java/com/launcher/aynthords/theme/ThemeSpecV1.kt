package com.launcher.aynthords.theme

import android.os.Build
import org.json.JSONArray
import org.json.JSONObject

private const val CURRENT_SCHEMA_VERSION = 1

data class ThemeMetadata(
    val name: String,
    val version: String,
    val author: String,
    val compatibility: ThemeCompatibility
)

data class ThemeCompatibility(
    val minSdk: Int,
    val maxSdk: Int?,
    val minAppVersion: String,
    val maxAppVersion: String?
)

data class ThemeColorTokens(
    val primary: String,
    val onPrimary: String,
    val secondary: String,
    val onSecondary: String,
    val tertiary: String,
    val onTertiary: String,
    val background: String,
    val onBackground: String,
    val surface: String,
    val onSurface: String
)

data class ThemePalette(
    val light: ThemeColorTokens,
    val dark: ThemeColorTokens
)

data class ThemeTextStyle(
    val fontSizeSp: Double,
    val lineHeightSp: Double,
    val letterSpacingSp: Double,
    val fontWeight: Int
)

data class ThemeTypography(
    val bodyLarge: ThemeTextStyle,
    val titleLarge: ThemeTextStyle,
    val labelSmall: ThemeTextStyle
)

data class ThemeMotion(
    val shortMs: Int,
    val mediumMs: Int,
    val longMs: Int,
    val easing: String
)

data class ThemeSurfaceLayout(
    val paddingDp: Double,
    val spacingDp: Double,
    val cornerRadiusDp: Double,
    val maxWidthDp: Double?
)

data class ThemeLayout(
    val interaction: ThemeSurfaceLayout,
    val presentation: ThemeSurfaceLayout
)

data class ThemeSpecV1(
    val schemaVersion: Int,
    val metadata: ThemeMetadata,
    val palette: ThemePalette,
    val typography: ThemeTypography,
    val motion: ThemeMotion,
    val layout: ThemeLayout
)

data class ThemeValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

data class ThemeParseResult(
    val spec: ThemeSpecV1,
    val validation: ThemeValidationResult,
    val usedFallback: Boolean
)

object ThemeSpecDefaults {
    val spec = ThemeSpecV1(
        schemaVersion = CURRENT_SCHEMA_VERSION,
        metadata = ThemeMetadata(
            name = "Thor Launcher Default",
            version = "1.0.0",
            author = "AynThor",
            compatibility = ThemeCompatibility(
                minSdk = 31,
                maxSdk = null,
                minAppVersion = "1.0.0",
                maxAppVersion = null
            )
        ),
        palette = ThemePalette(
            light = ThemeColorTokens(
                primary = "#6650A4",
                onPrimary = "#FFFFFF",
                secondary = "#625B71",
                onSecondary = "#FFFFFF",
                tertiary = "#7D5260",
                onTertiary = "#FFFFFF",
                background = "#FFFBFE",
                onBackground = "#1C1B1F",
                surface = "#FFFBFE",
                onSurface = "#1C1B1F"
            ),
            dark = ThemeColorTokens(
                primary = "#D0BCFF",
                onPrimary = "#381E72",
                secondary = "#CCC2DC",
                onSecondary = "#332D41",
                tertiary = "#EFB8C8",
                onTertiary = "#492532",
                background = "#1C1B1F",
                onBackground = "#E6E1E5",
                surface = "#1C1B1F",
                onSurface = "#E6E1E5"
            )
        ),
        typography = ThemeTypography(
            bodyLarge = ThemeTextStyle(16.0, 24.0, 0.5, 400),
            titleLarge = ThemeTextStyle(22.0, 28.0, 0.0, 500),
            labelSmall = ThemeTextStyle(11.0, 16.0, 0.5, 500)
        ),
        motion = ThemeMotion(
            shortMs = 120,
            mediumMs = 220,
            longMs = 360,
            easing = "fastOutSlowIn"
        ),
        layout = ThemeLayout(
            interaction = ThemeSurfaceLayout(16.0, 12.0, 12.0, null),
            presentation = ThemeSurfaceLayout(20.0, 16.0, 0.0, 720.0)
        )
    )
}

object ThemeSpecV1Parser {
    fun parse(raw: String): ThemeParseResult {
        val defaultSpec = ThemeSpecDefaults.spec
        return try {
            val json = JSONObject(raw)
            val validationErrors = mutableListOf<String>()
            val parsed = parseSpec(json, validationErrors)
            val validation = ThemeValidationResult(validationErrors.isEmpty(), validationErrors)
            if (!validation.isValid) {
                ThemeParseResult(defaultSpec, validation, usedFallback = true)
            } else {
                ThemeParseResult(parsed, validation, usedFallback = false)
            }
        } catch (e: Exception) {
            ThemeParseResult(
                defaultSpec,
                ThemeValidationResult(isValid = false, errors = listOf("Malformed JSON: ${e.message}")),
                usedFallback = true
            )
        }
    }

    private fun parseSpec(json: JSONObject, errors: MutableList<String>): ThemeSpecV1 {
        ensureAllowedKeys(
            json,
            setOf("schemaVersion", "metadata", "palette", "typography", "motion", "layout"),
            "root",
            errors
        )
        val schemaVersion = json.optInt("schemaVersion", -1)
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            errors += "Unsupported schemaVersion=$schemaVersion, expected $CURRENT_SCHEMA_VERSION"
        }

        val metadata = parseMetadata(json.optJSONObject("metadata"), errors)
        val palette = parsePalette(json.optJSONObject("palette"), errors)
        val typography = parseTypography(json.optJSONObject("typography"), errors)
        val motion = parseMotion(json.optJSONObject("motion"), errors)
        val layout = parseLayout(json.optJSONObject("layout"), errors)

        return ThemeSpecV1(schemaVersion, metadata, palette, typography, motion, layout)
    }

    private fun parseMetadata(obj: JSONObject?, errors: MutableList<String>): ThemeMetadata {
        val fallback = ThemeSpecDefaults.spec.metadata
        if (obj == null) {
            errors += "metadata missing"
            return fallback
        }
        ensureAllowedKeys(obj, setOf("name", "version", "author", "compatibility"), "metadata", errors)
        val name = requiredString(obj, "name", "metadata", errors) ?: fallback.name
        val version = requiredString(obj, "version", "metadata", errors) ?: fallback.version
        val author = requiredString(obj, "author", "metadata", errors) ?: fallback.author
        val compatibility = parseCompatibility(obj.optJSONObject("compatibility"), errors)
        return ThemeMetadata(name, version, author, compatibility)
    }

    private fun parseCompatibility(obj: JSONObject?, errors: MutableList<String>): ThemeCompatibility {
        val fallback = ThemeSpecDefaults.spec.metadata.compatibility
        if (obj == null) {
            errors += "metadata.compatibility missing"
            return fallback
        }
        ensureAllowedKeys(
            obj,
            setOf("minSdk", "maxSdk", "minAppVersion", "maxAppVersion"),
            "metadata.compatibility",
            errors
        )
        val minSdk = obj.optInt("minSdk", -1).also {
            if (it < 1) errors += "metadata.compatibility.minSdk must be >= 1"
        }
        val maxSdk = if (obj.has("maxSdk") && !obj.isNull("maxSdk")) obj.optInt("maxSdk", -1) else null
        if (maxSdk != null && maxSdk < minSdk) errors += "metadata.compatibility.maxSdk must be >= minSdk"
        val minApp = requiredString(obj, "minAppVersion", "metadata.compatibility", errors) ?: fallback.minAppVersion
        val maxApp = if (obj.has("maxAppVersion") && !obj.isNull("maxAppVersion")) {
            requiredString(obj, "maxAppVersion", "metadata.compatibility", errors)
        } else null
        return ThemeCompatibility(if (minSdk > 0) minSdk else fallback.minSdk, maxSdk, minApp, maxApp)
    }

    private fun parsePalette(obj: JSONObject?, errors: MutableList<String>): ThemePalette {
        val fallback = ThemeSpecDefaults.spec.palette
        if (obj == null) {
            errors += "palette missing"
            return fallback
        }
        ensureAllowedKeys(obj, setOf("light", "dark"), "palette", errors)
        return ThemePalette(
            light = parseColorTokens(obj.optJSONObject("light"), "palette.light", fallback.light, errors),
            dark = parseColorTokens(obj.optJSONObject("dark"), "palette.dark", fallback.dark, errors)
        )
    }

    private fun parseColorTokens(
        obj: JSONObject?,
        path: String,
        fallback: ThemeColorTokens,
        errors: MutableList<String>
    ): ThemeColorTokens {
        if (obj == null) {
            errors += "$path missing"
            return fallback
        }
        val keys = setOf(
            "primary", "onPrimary", "secondary", "onSecondary", "tertiary", "onTertiary",
            "background", "onBackground", "surface", "onSurface"
        )
        ensureAllowedKeys(obj, keys, path, errors)
        fun color(key: String, fb: String): String {
            val value = requiredString(obj, key, path, errors) ?: fb
            if (!Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$").matches(value)) {
                errors += "$path.$key must be hex color (#RRGGBB or #AARRGGBB)"
            }
            return value
        }

        return ThemeColorTokens(
            primary = color("primary", fallback.primary),
            onPrimary = color("onPrimary", fallback.onPrimary),
            secondary = color("secondary", fallback.secondary),
            onSecondary = color("onSecondary", fallback.onSecondary),
            tertiary = color("tertiary", fallback.tertiary),
            onTertiary = color("onTertiary", fallback.onTertiary),
            background = color("background", fallback.background),
            onBackground = color("onBackground", fallback.onBackground),
            surface = color("surface", fallback.surface),
            onSurface = color("onSurface", fallback.onSurface)
        )
    }

    private fun parseTypography(obj: JSONObject?, errors: MutableList<String>): ThemeTypography {
        val fallback = ThemeSpecDefaults.spec.typography
        if (obj == null) {
            errors += "typography missing"
            return fallback
        }
        ensureAllowedKeys(obj, setOf("bodyLarge", "titleLarge", "labelSmall"), "typography", errors)
        return ThemeTypography(
            bodyLarge = parseTextStyle(obj.optJSONObject("bodyLarge"), "typography.bodyLarge", fallback.bodyLarge, errors),
            titleLarge = parseTextStyle(obj.optJSONObject("titleLarge"), "typography.titleLarge", fallback.titleLarge, errors),
            labelSmall = parseTextStyle(obj.optJSONObject("labelSmall"), "typography.labelSmall", fallback.labelSmall, errors)
        )
    }

    private fun parseTextStyle(
        obj: JSONObject?,
        path: String,
        fallback: ThemeTextStyle,
        errors: MutableList<String>
    ): ThemeTextStyle {
        if (obj == null) {
            errors += "$path missing"
            return fallback
        }
        ensureAllowedKeys(obj, setOf("fontSizeSp", "lineHeightSp", "letterSpacingSp", "fontWeight"), path, errors)

        val fontSizeSp = obj.optDouble("fontSizeSp", -1.0)
        val lineHeightSp = obj.optDouble("lineHeightSp", -1.0)
        val letterSpacingSp = obj.optDouble("letterSpacingSp", Double.NaN)
        val fontWeight = obj.optInt("fontWeight", -1)

        if (fontSizeSp <= 0.0) errors += "$path.fontSizeSp must be > 0"
        if (lineHeightSp <= 0.0) errors += "$path.lineHeightSp must be > 0"
        if (letterSpacingSp.isNaN()) errors += "$path.letterSpacingSp missing"
        if (fontWeight !in 100..900) errors += "$path.fontWeight must be 100..900"

        return ThemeTextStyle(
            fontSizeSp = if (fontSizeSp > 0) fontSizeSp else fallback.fontSizeSp,
            lineHeightSp = if (lineHeightSp > 0) lineHeightSp else fallback.lineHeightSp,
            letterSpacingSp = if (!letterSpacingSp.isNaN()) letterSpacingSp else fallback.letterSpacingSp,
            fontWeight = if (fontWeight in 100..900) fontWeight else fallback.fontWeight
        )
    }

    private fun parseMotion(obj: JSONObject?, errors: MutableList<String>): ThemeMotion {
        val fallback = ThemeSpecDefaults.spec.motion
        if (obj == null) {
            errors += "motion missing"
            return fallback
        }
        ensureAllowedKeys(obj, setOf("shortMs", "mediumMs", "longMs", "easing"), "motion", errors)
        val shortMs = obj.optInt("shortMs", -1)
        val mediumMs = obj.optInt("mediumMs", -1)
        val longMs = obj.optInt("longMs", -1)
        val easing = requiredString(obj, "easing", "motion", errors) ?: fallback.easing

        if (shortMs !in 0..5_000) errors += "motion.shortMs must be 0..5000"
        if (mediumMs !in 0..5_000) errors += "motion.mediumMs must be 0..5000"
        if (longMs !in 0..10_000) errors += "motion.longMs must be 0..10000"
        if (!(shortMs <= mediumMs && mediumMs <= longMs)) errors += "motion duration ordering must be short <= medium <= long"

        return ThemeMotion(
            shortMs = if (shortMs in 0..5_000) shortMs else fallback.shortMs,
            mediumMs = if (mediumMs in 0..5_000) mediumMs else fallback.mediumMs,
            longMs = if (longMs in 0..10_000) longMs else fallback.longMs,
            easing = easing
        )
    }

    private fun parseLayout(obj: JSONObject?, errors: MutableList<String>): ThemeLayout {
        val fallback = ThemeSpecDefaults.spec.layout
        if (obj == null) {
            errors += "layout missing"
            return fallback
        }
        ensureAllowedKeys(obj, setOf("interaction", "presentation"), "layout", errors)
        return ThemeLayout(
            interaction = parseSurfaceLayout(
                obj.optJSONObject("interaction"),
                "layout.interaction",
                fallback.interaction,
                errors,
                allowMaxWidth = false
            ),
            presentation = parseSurfaceLayout(
                obj.optJSONObject("presentation"),
                "layout.presentation",
                fallback.presentation,
                errors,
                allowMaxWidth = true
            )
        )
    }

    private fun parseSurfaceLayout(
        obj: JSONObject?,
        path: String,
        fallback: ThemeSurfaceLayout,
        errors: MutableList<String>,
        allowMaxWidth: Boolean
    ): ThemeSurfaceLayout {
        if (obj == null) {
            errors += "$path missing"
            return fallback
        }
        val allowedKeys = if (allowMaxWidth) {
            setOf("paddingDp", "spacingDp", "cornerRadiusDp", "maxWidthDp")
        } else {
            setOf("paddingDp", "spacingDp", "cornerRadiusDp")
        }
        ensureAllowedKeys(obj, allowedKeys, path, errors)

        val padding = obj.optDouble("paddingDp", -1.0)
        val spacing = obj.optDouble("spacingDp", -1.0)
        val corner = obj.optDouble("cornerRadiusDp", -1.0)
        val maxWidth = if (allowMaxWidth && obj.has("maxWidthDp") && !obj.isNull("maxWidthDp")) {
            obj.optDouble("maxWidthDp", -1.0)
        } else null

        if (padding < 0.0) errors += "$path.paddingDp must be >= 0"
        if (spacing < 0.0) errors += "$path.spacingDp must be >= 0"
        if (corner < 0.0) errors += "$path.cornerRadiusDp must be >= 0"
        if (maxWidth != null && maxWidth <= 0.0) errors += "$path.maxWidthDp must be > 0 when provided"

        return ThemeSurfaceLayout(
            paddingDp = if (padding >= 0) padding else fallback.paddingDp,
            spacingDp = if (spacing >= 0) spacing else fallback.spacingDp,
            cornerRadiusDp = if (corner >= 0) corner else fallback.cornerRadiusDp,
            maxWidthDp = maxWidth ?: fallback.maxWidthDp
        )
    }

    private fun requiredString(
        obj: JSONObject,
        key: String,
        path: String,
        errors: MutableList<String>
    ): String? {
        if (!obj.has(key) || obj.isNull(key)) {
            errors += "$path.$key missing"
            return null
        }
        val value = obj.optString(key, "").trim()
        if (value.isEmpty()) {
            errors += "$path.$key must be a non-empty string"
            return null
        }
        return value
    }

    private fun ensureAllowedKeys(
        obj: JSONObject,
        allowed: Set<String>,
        path: String,
        errors: MutableList<String>
    ) {
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key !in allowed) {
                errors += "$path contains unsupported key '$key'"
            }
        }
    }
}

object ThemeCompatibilityChecker {
    fun isCompatible(spec: ThemeSpecV1, currentAppVersion: String): Boolean {
        val c = spec.metadata.compatibility
        if (Build.VERSION.SDK_INT < c.minSdk) return false
        if (c.maxSdk != null && Build.VERSION.SDK_INT > c.maxSdk) return false

        if (compareVersions(currentAppVersion, c.minAppVersion) < 0) return false
        if (c.maxAppVersion != null && compareVersions(currentAppVersion, c.maxAppVersion) > 0) return false
        return true
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val a = v1.split('.').mapNotNull { it.toIntOrNull() }
        val b = v2.split('.').mapNotNull { it.toIntOrNull() }
        val max = maxOf(a.size, b.size)
        for (i in 0 until max) {
            val ai = a.getOrElse(i) { 0 }
            val bi = b.getOrElse(i) { 0 }
            if (ai != bi) return ai.compareTo(bi)
        }
        return 0
    }
}
