package com.launcher.aynthords.domain.model

/**
 * Platform (e.g. PlayStation, Game Boy) from Daijishou-style platform config.
 * Identified by [uniqueId]; contains list of [players] that can launch content.
 */
data class Platform(
    val name: String,
    val uniqueId: String,
    val shortname: String,
    val description: String? = null,
    val acceptedFilenameRegex: String? = null,
    val players: List<Player>,
)

/**
 * Player (emulator or app) that can launch content for a platform.
 * [amStartArguments] is the am start–style argument string with placeholders
 * {file.path}, {file.uri}, {tags.<name>}.
 */
data class Player(
    val name: String,
    val uniqueId: String,
    val description: String? = null,
    val acceptedFilenameRegex: String? = null,
    val amStartArguments: String,
    val killPackageProcesses: Boolean = false,
    val killPackageProcessesWarning: Boolean = false,
    val extra: String? = null,
)
