package com.launcher.aynthords.data.local

import android.content.Context
import com.launcher.aynthords.domain.model.Platform
import com.launcher.aynthords.domain.model.Player
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

/**
 * Loads Daijishou-style platform JSON from assets and provides lookup by platformId / playerId.
 * Does not depend on Compose or UI.
 */
class PlatformRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private var platforms: List<Platform> = emptyList()
        get() {
            if (field.isEmpty()) {
                field = loadAllFromAssets()
            }
            return field
        }

    fun getPlatform(platformId: String): Platform? =
        platforms.find { it.uniqueId == platformId }

    fun getPlayer(platformId: String, playerId: String): Player? =
        getPlatform(platformId)?.players?.find { it.uniqueId == playerId }

    fun getAllPlatforms(): List<Platform> = platforms

    private fun loadAllFromAssets(): List<Platform> {
        val list = mutableListOf<Platform>()
        val path = "platforms"
        runCatching {
            (context.assets.list(path) ?: emptyArray()).forEach { name ->
                if (name.endsWith(".json")) {
                    context.assets.open("$path/$name").use { stream ->
                        val dto = json.decodeFromString<PlatformFileDto>(
                            InputStreamReader(stream).readText()
                        )
                        list.add(dto.toDomain())
                    }
                }
            }
        }
        return list
    }

    private fun PlatformFileDto.toDomain(): Platform =
        Platform(
            name = platform.name,
            uniqueId = platform.uniqueId,
            shortname = platform.shortname,
            description = platform.description,
            acceptedFilenameRegex = platform.acceptedFilenameRegex,
            players = playerList.map { it.toDomain() },
        )

    private fun PlayerDto.toDomain(): Player =
        Player(
            name = name,
            uniqueId = uniqueId,
            description = description,
            acceptedFilenameRegex = acceptedFilenameRegex,
            amStartArguments = amStartArguments,
            killPackageProcesses = killPackageProcesses,
            killPackageProcessesWarning = killPackageProcessesWarning,
            extra = extra,
        )
}
