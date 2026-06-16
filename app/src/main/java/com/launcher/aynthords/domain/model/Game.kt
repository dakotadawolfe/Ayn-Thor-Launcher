package com.launcher.aynthords.domain.model

data class Game(
    val id: String,
    val title: String,
    val heroArtUrl: String,
    val backgroundArtUrl: String,
    /** Absolute file path for launch (e.g. ROM path). */
    val filePath: String? = null,
    /** Content URI for launch when using content resolver. */
    val fileUri: String? = null,
    /** Platform uniqueId from Daijishou-style platform JSON. */
    val platformId: String? = null,
    /** Player uniqueId from platform's playerList. */
    val playerId: String? = null,
)
