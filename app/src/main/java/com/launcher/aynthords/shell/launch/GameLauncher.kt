package com.launcher.aynthords.shell.launch

import android.content.Context
import android.content.Intent
import com.launcher.aynthords.data.local.PlatformRepository
import com.launcher.aynthords.domain.model.Game
import java.io.File

/**
 * Builds a launch Intent for a [Game] using Daijishou-style platform/player config,
 * template substitution, and optional .dpt tag files. Does not depend on Compose.
 */
class GameLauncher(private val context: Context) {

    private val platformRepository = PlatformRepository(context)

    /**
     * Build an Intent to launch the given [game], or null if the game has no
     * platform/player or no launch data.
     */
    fun buildLaunchIntent(game: Game): Intent? {
        val platformId = game.platformId ?: return null
        val playerId = game.playerId ?: return null
        val player = platformRepository.getPlayer(platformId, playerId) ?: return null

        val filePath = game.filePath
        val fileUri = game.fileUri

        val tags = if (filePath != null) loadDptTagsForFile(filePath) else emptyMap()

        return AmStartIntentBuilder.buildIntent(
            template = player.amStartArguments,
            filePath = filePath,
            fileUri = fileUri,
            tags = tags,
        )
    }

    /**
     * Look for a .dpt file next to [gameFilePath] (same directory, same base name, .dpt extension)
     * and parse it. Returns empty map if not found or parse fails.
     */
    private fun loadDptTagsForFile(gameFilePath: String): Map<String, String> {
        val file = File(gameFilePath)
        val parent = file.parent ?: return emptyMap()
        val baseName = file.nameWithoutExtension
        val dptFile = File(parent, "$baseName.dpt")
        if (!dptFile.canRead()) return emptyMap()
        return runCatching { DptParser.parse(dptFile.readText()) }.getOrElse { emptyMap() }
    }
}
