package com.launcher.aynthords.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.launcher.aynthords.domain.model.ArtworkOverrides
import com.launcher.aynthords.domain.model.GameProfile
import com.launcher.aynthords.domain.model.LaunchPolicyOverride
import com.launcher.aynthords.domain.model.SurfaceRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.gameProfileDataStore by preferencesDataStore(name = "game_profiles")

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private const val CURRENT_SCHEMA_VERSION = 1

class GameProfileStore(context: Context) {
    private val appContext = context.applicationContext

    private fun profileKey(id: String) = stringPreferencesKey("profile_$id")

    val profiles: Flow<Map<String, GameProfile>> = appContext.gameProfileDataStore.data.map { prefs ->
        prefs.asMap().entries
            .filter { it.key.name.startsWith("profile_") }
            .mapNotNull { entry ->
                val key = entry.key.name
                val id = key.removePrefix("profile_")
                val value = entry.value as? String ?: return@mapNotNull null
                try {
                    val parsed = json.decodeFromString<GameProfileDto>(value)
                    parsed.toGameProfile(id)
                } catch (_: Exception) {
                    null
                }
            }
            .associateBy { it.id }
    }

    suspend fun getProfile(id: String): GameProfile? {
        return appContext.gameProfileDataStore.data.map { prefs ->
            prefs[profileKey(id)]?.let { json.decodeFromString<GameProfileDto>(it).toGameProfile(id) }
        }.first()
    }

    suspend fun putProfile(profile: GameProfile) {
        appContext.gameProfileDataStore.edit { prefs ->
            prefs[profileKey(profile.id)] = json.encodeToString(toDto(profile))
        }
    }

    suspend fun removeProfile(id: String) {
        appContext.gameProfileDataStore.edit { prefs ->
            prefs.remove(profileKey(id))
        }
    }

    private fun GameProfileDto.toGameProfile(id: String): GameProfile {
        val schemaVersion = schemaVersion ?: 1
        return GameProfile(
            id = id,
            artworkOverrides = ArtworkOverrides(
                hero = artworkOverrides?.hero,
                background = artworkOverrides?.background,
                poster = artworkOverrides?.poster,
            ),
            tags = tags?.toSet() ?: emptySet(),
            collections = collections?.toSet() ?: emptySet(),
            launchPolicyOverride = launchPolicyOverride?.let {
                LaunchPolicyOverride(
                    preferredLogicalRole = it.preferredLogicalRole?.let { r ->
                        SurfaceRole.entries.find { e -> e.name == r } ?: SurfaceRole.INTERACTION
                    } ?: SurfaceRole.INTERACTION,
                    launchFlags = it.launchFlags?.toSet() ?: emptySet(),
                )
            },
            themeOverrideId = themeOverrideId,
            pinnedOrderOverride = pinnedOrderOverride,
            notes = notes ?: "",
            isHidden = isHidden,
            isFavorite = isFavorite,
            schemaVersion = schemaVersion.coerceAtMost(CURRENT_SCHEMA_VERSION),
        )
    }
}

@kotlinx.serialization.Serializable
private data class GameProfileDto(
    val artworkOverrides: ArtworkOverridesDto? = null,
    val tags: List<String>? = null,
    val collections: List<String>? = null,
    val launchPolicyOverride: LaunchPolicyOverrideDto? = null,
    val themeOverrideId: String? = null,
    val pinnedOrderOverride: Int? = null,
    val notes: String? = null,
    val isHidden: Boolean? = null,
    val isFavorite: Boolean? = null,
    val schemaVersion: Int? = null,
)

@kotlinx.serialization.Serializable
private data class ArtworkOverridesDto(
    val hero: String? = null,
    val background: String? = null,
    val poster: String? = null,
)

@kotlinx.serialization.Serializable
private data class LaunchPolicyOverrideDto(
    val preferredLogicalRole: String? = null,
    val launchFlags: List<String>? = null,
)

private fun toDto(p: GameProfile): GameProfileDto = GameProfileDto(
    artworkOverrides = ArtworkOverridesDto(
        hero = p.artworkOverrides.hero,
        background = p.artworkOverrides.background,
        poster = p.artworkOverrides.poster,
    ),
    tags = p.tags.toList(),
    collections = p.collections.toList(),
    launchPolicyOverride = p.launchPolicyOverride?.let {
        LaunchPolicyOverrideDto(
            preferredLogicalRole = it.preferredLogicalRole.name,
            launchFlags = it.launchFlags.toList(),
        )
    },
    themeOverrideId = p.themeOverrideId,
    pinnedOrderOverride = p.pinnedOrderOverride,
    notes = p.notes.takeIf { it.isNotEmpty() },
    isHidden = p.isHidden,
    isFavorite = p.isFavorite,
    schemaVersion = p.schemaVersion,
)
