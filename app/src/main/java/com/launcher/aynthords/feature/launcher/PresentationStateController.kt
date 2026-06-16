package com.launcher.aynthords.feature.launcher

import com.launcher.aynthords.domain.model.PresentationLoadingState
import com.launcher.aynthords.domain.model.PresentationMetadata
import com.launcher.aynthords.domain.model.PresentationUiState

/**
 * Pure projector that derives [PresentationUiState] from [LauncherUiState].
 *
 * Responsibility:
 * - Decide which entry should drive presentation (focus vs selection).
 * - Shape a stable, theme-friendly model for hero/background/metadata.
 * - Encode simple loading/empty semantics.
 *
 * It does **not** mutate launcher state or depend on Compose/Android APIs.
 */
object PresentationStateController {

    /**
     * Derive a [PresentationUiState] from the current [LauncherUiState].
     *
     * Rules (V1):
     * - If a focused entry exists, it drives presentation.
     * - Otherwise, fall back to the selected entry.
     * - If no entry exists, we are in [PresentationLoadingState.EMPTY].
     * - When an entry is present, we use [PresentationLoadingState.IDLE].
     */
    fun derive(ui: LauncherUiState): PresentationUiState {
        val focused = ui.focusedEntry
        val selected = ui.selectedEntry
        val drivingEntry = focused ?: selected

        val loadingState = if (drivingEntry == null) {
            PresentationLoadingState.EMPTY
        } else {
            PresentationLoadingState.IDLE
        }

        if (drivingEntry == null) {
            return PresentationUiState(
                focusedGameId = ui.focusedEntryId,
                selectedGameId = ui.selectedEntryId,
                loadingState = loadingState,
            )
        }

        val metadata = PresentationMetadata(
            genre = null,
            lastPlayed = null,
            approximatePlaytimeHours = null,
        )

        return PresentationUiState(
            focusedGameId = ui.focusedEntryId,
            selectedGameId = ui.selectedEntryId,
            heroArtUrl = drivingEntry.heroArtUrl,
            backgroundArtUrl = drivingEntry.backgroundArtUrl,
            title = drivingEntry.label,
            subtitle = null,
            metadata = metadata,
            loadingState = loadingState,
        )
    }
}
