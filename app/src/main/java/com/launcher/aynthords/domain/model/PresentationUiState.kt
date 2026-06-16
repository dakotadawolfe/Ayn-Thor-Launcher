package com.launcher.aynthords.domain.model

/**
 * Logical view-model for the presentation surface.
 *
 * This is intentionally decoupled from Compose and from the full [Game] model so that
 * themes can render rich presentation UI without owning selection or data providers.
 *
 * Input:
 * - [focusedGameId] / [selectedGameId] from [LauncherUiState].
 *
 * Output:
 * - Stable fields describing what the presentation surface should show and in which
 *   loading/error state it is.
 *
 * Invariants:
 * - Presentation never mutates selection; it only reflects launcher state.
 */
data class PresentationUiState(
    val focusedGameId: String? = null,
    val selectedGameId: String? = null,
    val heroArtUrl: String? = null,
    val backgroundArtUrl: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val metadata: PresentationMetadata? = null,
    val loadingState: PresentationLoadingState = PresentationLoadingState.EMPTY,
)

/**
 * Minimal metadata model for presentation, intentionally placeholder-friendly so it can
 * evolve into a richer content pipeline later.
 */
data class PresentationMetadata(
    val genre: String? = null,
    val lastPlayed: String? = null,
    val approximatePlaytimeHours: Int? = null,
)

enum class PresentationLoadingState {
    IDLE,
    LOADING,
    ERROR,
    EMPTY,
}

