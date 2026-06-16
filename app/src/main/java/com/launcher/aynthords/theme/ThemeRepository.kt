package com.launcher.aynthords.theme

import android.content.Context
import android.util.Log
import com.launcher.aynthords.theme.spec.LayoutSpecV1
import com.launcher.aynthords.theme.spec.ThemeSpecV1
import com.launcher.aynthords.ui.theme.Theme
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

private const val TAG = "ThemeRepository"

object ThemeRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun loadActiveTheme(): Theme = Theme("default")

    /**
     * Shallow merge: base + override (override wins). Used for theme set + system overrides and includes.
     */
    private fun mergeJsonObjects(base: JsonObject, override: JsonObject): JsonObject =
        JsonObject(base + override)

    /**
     * Loads layout spec for the given theme set and optional platform.
     * Paths: themes/<themeSetId>/layout.json; optional themes/<themeSetId>/systems/<platformId>/layout.json (override).
     * If theme layout JSON has "include" key, that theme's layout is loaded first and merged (current wins).
     */
    fun loadLayoutSpec(context: Context, themeSetId: String = "default", platformId: String? = null): LayoutSpecV1 {
        return try {
            var obj = loadLayoutJsonObject(context, "themes/$themeSetId/layout.json") ?: return LayoutSpecV1(schemaVersion = 1, layouts = emptyMap())
            if (platformId != null) {
                val override = loadLayoutJsonObject(context, "themes/$themeSetId/systems/$platformId/layout.json")
                if (override != null) obj = mergeJsonObjects(obj, override)
            }
            json.decodeFromJsonElement(serializer<LayoutSpecV1>(), obj)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load layout spec", e)
            LayoutSpecV1(schemaVersion = 1, layouts = emptyMap())
        }
    }

    private fun loadLayoutJsonObject(context: Context, path: String): JsonObject? {
        return try {
            val text = context.assets.open(path).bufferedReader().use { it.readText() }
            val obj = json.parseToJsonElement(text).jsonObject
            if (obj.containsKey("include")) {
                val incId = obj["include"]!!.jsonPrimitive.content
                val incPath = "themes/$incId/layout.json"
                val inc = loadLayoutJsonObject(context, incPath) ?: return obj
                mergeJsonObjects(inc, obj)
            } else obj
        } catch (_: Exception) { null }
    }

    /**
     * Loads theme spec for the given theme set and optional platform.
     * Paths: themes/<themeSetId>/theme.json; optional themes/<themeSetId>/systems/<platformId>/theme.json (override).
     * If theme JSON has "include" key, that theme's spec is loaded first and merged (current wins).
     */
    fun loadThemeSpec(context: Context, themeSetId: String = "default", platformId: String? = null): ThemeSpecV1 {
        return try {
            var obj = loadThemeJsonObject(context, "themes/$themeSetId/theme.json") ?: return ThemeSpecV1()
            if (platformId != null) {
                val override = loadThemeJsonObject(context, "themes/$themeSetId/systems/$platformId/theme.json")
                if (override != null) obj = mergeJsonObjects(obj, override)
            }
            json.decodeFromJsonElement(serializer<ThemeSpecV1>(), obj)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load theme spec", e)
            ThemeSpecV1()
        }
    }

    private fun loadThemeJsonObject(context: Context, path: String): JsonObject? {
        return try {
            val text = context.assets.open(path).bufferedReader().use { it.readText() }
            val obj = json.parseToJsonElement(text).jsonObject
            if (obj.containsKey("include")) {
                val incId = obj["include"]!!.jsonPrimitive.content
                val incPath = "themes/$incId/theme.json"
                val inc = loadThemeJsonObject(context, incPath) ?: return obj
                mergeJsonObjects(inc, obj)
            } else obj
        } catch (_: Exception) { null }
    }
}
