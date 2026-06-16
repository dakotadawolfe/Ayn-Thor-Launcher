package com.launcher.aynthords.data.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Root of Daijishou-style platform JSON file. */
@Serializable
data class PlatformFileDto(
    val databaseVersion: Int? = null,
    val revisionNumber: Int? = null,
    val platform: PlatformDto,
    @SerialName("playerList") val playerList: List<PlayerDto>,
)

@Serializable
data class PlatformDto(
    val name: String,
    @SerialName("uniqueId") val uniqueId: String,
    val shortname: String,
    val description: String? = null,
    val acceptedFilenameRegex: String? = null,
    val scraperSourceList: List<String>? = null,
    val boxArtAspectRatioId: Int? = null,
    val useCustomBoxArtAspectRatio: Boolean? = null,
    val customBoxArtAspectRatio: String? = null,
    val screenAspectRatioId: Int? = null,
    val useCustomScreenAspectRatio: Boolean? = null,
    val customScreenAspectRatio: String? = null,
    val retroAchievementsAlias: String? = null,
    val retroAchievementsConsoleIdList: List<Int>? = null,
    val extra: String? = null,
)

@Serializable
data class PlayerDto(
    val name: String,
    @SerialName("uniqueId") val uniqueId: String,
    val description: String? = null,
    val acceptedFilenameRegex: String? = null,
    @SerialName("amStartArguments") val amStartArguments: String,
    val killPackageProcesses: Boolean = false,
    val killPackageProcessesWarning: Boolean = false,
    val extra: String? = null,
)
