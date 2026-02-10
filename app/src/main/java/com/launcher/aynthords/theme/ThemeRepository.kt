package com.launcher.aynthords.theme

import android.content.Context
import android.util.Log
import com.launcher.aynthords.BuildConfig
import java.io.File

data class ThemeSource(
    val id: String,
    val location: String,
    val spec: ThemeSpecV1,
    val metadata: ThemeMetadata,
    val usedFallback: Boolean,
    val validationErrors: List<String>
)

object ThemeRepository {
    private const val TAG = "ThemeRepository"
    private const val ASSET_THEME_DIR = "themes"
    private const val EXTERNAL_THEME_SUBDIR = "themes"

    fun discoverThemes(context: Context): List<ThemeSource> {
        val discovered = mutableListOf<ThemeSource>()
        discovered += discoverAssetThemes(context)
        discovered += discoverFileThemes(context.filesDir.resolve(EXTERNAL_THEME_SUBDIR), "internal")
        context.getExternalFilesDir(null)?.let {
            discovered += discoverFileThemes(it.resolve(EXTERNAL_THEME_SUBDIR), "external")
        }
        return discovered
    }

    fun loadBestTheme(context: Context): ThemeSource {
        val discovered = discoverThemes(context)
        return discovered.firstOrNull {
            ThemeCompatibilityChecker.isCompatible(it.spec, BuildConfig.VERSION_NAME)
        } ?: ThemeSource(
            id = "fallback-default",
            location = "in-memory default",
            spec = ThemeSpecDefaults.spec,
            metadata = ThemeSpecDefaults.spec.metadata,
            usedFallback = true,
            validationErrors = listOf("No compatible theme file discovered")
        )
    }

    private fun discoverAssetThemes(context: Context): List<ThemeSource> {
        return try {
            context.assets.list(ASSET_THEME_DIR)
                ?.filter { it.endsWith(".json", ignoreCase = true) }
                ?.mapNotNull { fileName ->
                    val raw = context.assets.open("$ASSET_THEME_DIR/$fileName").bufferedReader().use { it.readText() }
                    toSource(raw, "asset:$ASSET_THEME_DIR/$fileName")
                }
                ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed discovering asset themes", e)
            emptyList()
        }
    }

    private fun discoverFileThemes(directory: File, prefix: String): List<ThemeSource> {
        if (!directory.exists() || !directory.isDirectory) return emptyList()
        return directory.listFiles()
            ?.filter { it.isFile && it.extension.equals("json", ignoreCase = true) }
            ?.mapNotNull { file ->
                val raw = runCatching { file.readText() }.getOrElse {
                    Log.w(TAG, "Cannot read theme file: ${file.absolutePath}", it)
                    return@mapNotNull null
                }
                toSource(raw, "$prefix:${file.absolutePath}")
            }
            ?: emptyList()
    }

    private fun toSource(raw: String, location: String): ThemeSource? {
        val parsed = ThemeSpecV1Parser.parse(raw)
        val spec = parsed.spec
        val metadata = spec.metadata
        return ThemeSource(
            id = "${metadata.name}-${metadata.version}",
            location = location,
            spec = spec,
            metadata = metadata,
            usedFallback = parsed.usedFallback,
            validationErrors = parsed.validation.errors
        )
    }
}
