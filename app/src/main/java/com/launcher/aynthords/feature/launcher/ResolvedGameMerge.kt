package com.launcher.aynthords.feature.launcher

import com.launcher.aynthords.domain.model.GameProfile
import com.launcher.aynthords.domain.model.LibraryEntry
import com.launcher.aynthords.domain.model.ResolvedGameModel

/**
 * Pure merge of [LibraryEntry] + [GameProfile] into [ResolvedGameModel].
 *
 * Profile overrides win; when profile is null, use entry defaults.
 * Artwork: profile overrides are resolved via [artworkResolver]; fallback to entry icon.
 */
fun merge(
    entry: LibraryEntry,
    profile: GameProfile?,
    artworkResolver: (packageName: String, key: String) -> String?,
): ResolvedGameModel {
    val fallbackUrl = entry.heroArtUrl

    val (heroArtUrl, backgroundArtUrl, posterArtUrl) = if (profile != null) {
        Triple(
            profile.artworkOverrides.hero?.let { artworkResolver(entry.packageName, it) } ?: fallbackUrl,
            profile.artworkOverrides.background?.let { artworkResolver(entry.packageName, it) } ?: fallbackUrl,
            profile.artworkOverrides.poster?.let { artworkResolver(entry.packageName, it) },
        )
    } else {
        Triple(fallbackUrl, fallbackUrl, null)
    }

    val isHidden = profile?.isHidden ?: entry.isHidden
    val isFavorite = profile?.isFavorite ?: entry.isFavorite
    val collections = if (profile != null && profile.collections.isNotEmpty())
        profile.collections else entry.collections
    val tags = profile?.tags ?: emptySet()

    return ResolvedGameModel(
        id = entry.id,
        packageName = entry.packageName,
        label = entry.label,
        iconCacheKey = entry.iconCacheKey,
        isGame = entry.isGame,
        isSystem = entry.isSystem,
        isHidden = isHidden,
        isFavorite = isFavorite,
        collections = collections,
        lastLaunchedAt = entry.lastLaunchedAt,
        heroArtUrl = heroArtUrl,
        backgroundArtUrl = backgroundArtUrl,
        posterArtUrl = posterArtUrl,
        tags = tags,
        launchPolicyOverride = profile?.launchPolicyOverride,
        themeOverrideId = profile?.themeOverrideId,
        pinnedOrderOverride = profile?.pinnedOrderOverride,
        notes = profile?.notes ?: "",
    )
}
