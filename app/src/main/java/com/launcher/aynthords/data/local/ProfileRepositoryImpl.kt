package com.launcher.aynthords.data.local

import com.launcher.aynthords.domain.model.ArtworkOverrides
import com.launcher.aynthords.domain.model.GameProfile
import com.launcher.aynthords.domain.repo.ProfileField
import com.launcher.aynthords.domain.repo.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepositoryImpl(
    private val store: GameProfileStore,
) : ProfileRepository {

    override fun observeProfiles(): Flow<Map<String, GameProfile>> = store.profiles

    override fun observeProfile(id: String): Flow<GameProfile?> =
        store.profiles.map { it[id] }

    override suspend fun updateProfile(id: String, patch: (GameProfile) -> GameProfile) {
        val current = store.getProfile(id) ?: GameProfile(id = id)
        val updated = patch(current)
        store.putProfile(updated)
    }

    override suspend fun clearProfileField(id: String, field: ProfileField) {
        val current = store.getProfile(id) ?: return
        val updated = when (field) {
            ProfileField.HeroArt -> current.copy(
                artworkOverrides = current.artworkOverrides.copy(hero = null)
            )
            ProfileField.BackgroundArt -> current.copy(
                artworkOverrides = current.artworkOverrides.copy(background = null)
            )
            ProfileField.PosterArt -> current.copy(
                artworkOverrides = current.artworkOverrides.copy(poster = null)
            )
            ProfileField.Tags -> current.copy(tags = emptySet())
            ProfileField.Collections -> current.copy(collections = emptySet())
            ProfileField.LaunchPolicy -> current.copy(launchPolicyOverride = null)
            ProfileField.ThemeOverride -> current.copy(themeOverrideId = null)
            ProfileField.PinnedOrder -> current.copy(pinnedOrderOverride = null)
            ProfileField.Notes -> current.copy(notes = "")
            ProfileField.Hidden -> current.copy(isHidden = null)
            ProfileField.Favorite -> current.copy(isFavorite = null)
        }
        store.putProfile(updated)
    }
}
