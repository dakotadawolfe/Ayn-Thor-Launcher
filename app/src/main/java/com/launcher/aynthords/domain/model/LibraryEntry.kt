package com.launcher.aynthords.domain.model

/**
 * Normalized library entry from PackageManager.
 *
 * Launch intent is resolved at launch time by LaunchController to avoid
 * serialization; do not store Intent in DataStore.
 */
data class LibraryEntry(
    val id: String,
    val packageName: String,
    val label: String,
    val iconCacheKey: String,
    val isGame: Boolean,
    val isSystem: Boolean,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false,
    val collections: Set<String> = emptySet(),
    val lastLaunchedAt: Long? = null,
) {
    /** Display-friendly key for Coil (e.g. "pkg:com.example.app"). */
    val heroArtUrl: String get() = iconCacheKey
}
