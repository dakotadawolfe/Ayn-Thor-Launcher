package com.launcher.aynthords.data.local

import java.io.File

enum class ArtworkType {
    Hero,
    Background,
    Poster,
}

object ArtworkStorage {
    private const val ARTWORK_DIR = "artwork"
    private const val EXTENSION = "jpg"
    private const val MAX_TOTAL_BYTES = 50L * 1024 * 1024 // 50MB

    fun pathFor(filesDir: File, packageName: String, type: ArtworkType): File {
        val dir = File(filesDir, "$ARTWORK_DIR/$packageName")
        dir.mkdirs()
        return File(dir, "${type.name.lowercase()}.$EXTENSION")
    }

    fun keyFor(type: ArtworkType): String = type.name.lowercase()

    fun keyToType(key: String): ArtworkType = when (key.lowercase()) {
        "hero" -> ArtworkType.Hero
        "background" -> ArtworkType.Background
        "poster" -> ArtworkType.Poster
        else -> ArtworkType.Hero
    }

    /**
     * Evict oldest artwork files until total size is under [MAX_TOTAL_BYTES].
     * Call after adding new artwork.
     */
    fun evictIfNeeded(filesDir: File) {
        val artworkRoot = File(filesDir, ARTWORK_DIR)
        if (!artworkRoot.exists()) return

        val allFiles = artworkRoot.walkTopDown()
            .filter { it.isFile && it.extension.equals(EXTENSION, ignoreCase = true) }
            .map { it to it.lastModified() }
            .toList()
            .sortedBy { it.second }

        var totalBytes = allFiles.sumOf { it.first.length() }
        for ((file, _) in allFiles) {
            if (totalBytes <= MAX_TOTAL_BYTES) break
            totalBytes -= file.length()
            file.delete()
        }
    }
}
