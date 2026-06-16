package com.launcher.aynthords.feature.launcher

import com.launcher.aynthords.domain.model.ResolvedGameModel
import com.launcher.aynthords.domain.model.PresentationLoadingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PresentationStateControllerTest {

    private fun entry(id: String, label: String, heroArtUrl: String, backgroundArtUrl: String = heroArtUrl) = ResolvedGameModel(
        id = id,
        packageName = id,
        label = label,
        iconCacheKey = heroArtUrl,
        isGame = false,
        isSystem = false,
        isHidden = false,
        isFavorite = false,
        collections = emptySet(),
        lastLaunchedAt = null,
        heroArtUrl = heroArtUrl,
        backgroundArtUrl = backgroundArtUrl,
        posterArtUrl = null,
        tags = emptySet(),
        launchPolicyOverride = null,
        themeOverrideId = null,
        pinnedOrderOverride = null,
        notes = "",
    )

    @Test
    fun `derive returns EMPTY when no entries`() {
        val uiState = LauncherUiState(
            entries = emptyList(),
            focusedEntryId = null,
            selectedEntryId = null,
        )

        val result = PresentationStateController.derive(uiState)

        assertEquals(PresentationLoadingState.EMPTY, result.loadingState)
        assertNull(result.heroArtUrl)
        assertNull(result.backgroundArtUrl)
        assertNull(result.title)
    }

    @Test
    fun `derive prefers focused entry over selected`() {
        val first = entry("pkg1", "First App", "pkg:pkg1")
        val second = entry("pkg2", "Second App", "pkg:pkg2")

        val uiState = LauncherUiState(
            entries = listOf(first, second),
            focusedEntryId = second.id,
            selectedEntryId = first.id,
        )

        val result = PresentationStateController.derive(uiState)

        assertEquals(PresentationLoadingState.IDLE, result.loadingState)
        assertEquals(second.heroArtUrl, result.heroArtUrl)
        assertEquals(second.backgroundArtUrl, result.backgroundArtUrl)
        assertEquals(second.label, result.title)
    }

    @Test
    fun `derive falls back to selected when no focus`() {
        val first = entry("pkg1", "First App", "pkg:pkg1")

        val uiState = LauncherUiState(
            entries = listOf(first),
            focusedEntryId = null,
            selectedEntryId = first.id,
        )

        val result = PresentationStateController.derive(uiState)

        assertEquals(PresentationLoadingState.IDLE, result.loadingState)
        assertEquals(first.heroArtUrl, result.heroArtUrl)
        assertEquals(first.backgroundArtUrl, result.backgroundArtUrl)
        assertEquals(first.label, result.title)
    }
}
