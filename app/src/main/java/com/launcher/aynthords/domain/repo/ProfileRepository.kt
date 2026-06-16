package com.launcher.aynthords.domain.repo

import com.launcher.aynthords.domain.model.GameProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfiles(): Flow<Map<String, GameProfile>>
    fun observeProfile(id: String): Flow<GameProfile?>
    suspend fun updateProfile(id: String, patch: (GameProfile) -> GameProfile)
    suspend fun clearProfileField(id: String, field: ProfileField)
}

enum class ProfileField {
    HeroArt,
    BackgroundArt,
    PosterArt,
    Tags,
    Collections,
    LaunchPolicy,
    ThemeOverride,
    PinnedOrder,
    Notes,
    Hidden,
    Favorite,
}
