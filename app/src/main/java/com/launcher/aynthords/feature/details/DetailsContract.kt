package com.launcher.aynthords.feature.details

import com.launcher.aynthords.domain.model.ResolvedGameModel

/**
 * Consistent contract for the Details view (ES-style "detailed" view).
 * Themes decide where/how these appear via props and layout.
 *
 * Today the domain is app-only; extend with platform, emulator, romPath when that domain exists.
 */
data class DetailsContract(
    val title: String,
    val platform: String? = null,
    val emulator: String? = null,
    val romPath: String? = null,
    val notes: String,
    val heroArtUrl: String,
    val backgroundArtUrl: String,
    val posterArtUrl: String?,
)

fun ResolvedGameModel.toDetailsContract(): DetailsContract = DetailsContract(
    title = label,
    platform = null,
    emulator = null,
    romPath = null,
    notes = notes,
    heroArtUrl = heroArtUrl,
    backgroundArtUrl = backgroundArtUrl,
    posterArtUrl = posterArtUrl,
)
