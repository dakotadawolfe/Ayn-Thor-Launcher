package com.launcher.aynthords.domain.model

/**
 * Merged view of [LibraryEntry] + [GameProfile] for UI consumption.
 *
 * UI only consumes ResolvedGameModel; artwork URLs are resolved at merge time.
 */
data class ResolvedGameModel(
    val id: String,
    val packageName: String,
    val label: String,
    val iconCacheKey: String,
    val isGame: Boolean,
    val isSystem: Boolean,
    val isHidden: Boolean,
    val isFavorite: Boolean,
    val collections: Set<String>,
    val lastLaunchedAt: Long?,
    val heroArtUrl: String,
    val backgroundArtUrl: String,
    val posterArtUrl: String?,
    val tags: Set<String>,
    val launchPolicyOverride: LaunchPolicyOverride?,
    val themeOverrideId: String?,
    val pinnedOrderOverride: Int?,
    val notes: String,
)
