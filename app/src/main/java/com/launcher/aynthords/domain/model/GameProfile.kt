package com.launcher.aynthords.domain.model

import kotlinx.serialization.Serializable

/**
 * Per-title profile keyed by stable library id (packageName).
 *
 * Profile overrides curation (favorites, hidden, collections) when present.
 * Artwork overrides use internal file keys; resolved to URIs at merge time.
 */
@Serializable
data class GameProfile(
    val id: String,
    val artworkOverrides: ArtworkOverrides = ArtworkOverrides(),
    val tags: Set<String> = emptySet(),
    val collections: Set<String> = emptySet(),
    val launchPolicyOverride: LaunchPolicyOverride? = null,
    val themeOverrideId: String? = null,
    val pinnedOrderOverride: Int? = null,
    val notes: String = "",
    val isHidden: Boolean? = null,
    val isFavorite: Boolean? = null,
    val schemaVersion: Int = 1,
)

@Serializable
data class ArtworkOverrides(
    val hero: String? = null,
    val background: String? = null,
    val poster: String? = null,
)

@Serializable
data class LaunchPolicyOverride(
    val preferredLogicalRole: SurfaceRole = SurfaceRole.INTERACTION,
    val launchFlags: Set<String> = emptySet(),
)
