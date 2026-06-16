package com.launcher.aynthords.feature.launcher

import com.launcher.aynthords.domain.model.ArtworkOverrides
import com.launcher.aynthords.domain.model.GameProfile
import com.launcher.aynthords.domain.model.LaunchPolicyOverride
import com.launcher.aynthords.domain.model.LibraryEntry
import com.launcher.aynthords.domain.model.SurfaceRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolvedGameMergeTest {

    private val defaultEntry = LibraryEntry(
        id = "com.example.game",
        packageName = "com.example.game",
        label = "Test Game",
        iconCacheKey = "pkg:com.example.game",
        isGame = true,
        isSystem = false,
        isHidden = false,
        isFavorite = false,
        collections = emptySet(),
        lastLaunchedAt = 12345L,
    )

    private val resolver: (String, String) -> String? = { pkg, key ->
        "file:///artwork/$pkg/$key.jpg"
    }

    @Test
    fun `merge with null profile uses entry defaults`() {
        val result = merge(defaultEntry, null, resolver)

        assertEquals("com.example.game", result.id)
        assertEquals("Test Game", result.label)
        assertEquals("pkg:com.example.game", result.heroArtUrl)
        assertEquals("pkg:com.example.game", result.backgroundArtUrl)
        assertNull(result.posterArtUrl)
        assertEquals(false, result.isHidden)
        assertEquals(false, result.isFavorite)
        assertEquals(emptySet<String>(), result.collections)
        assertEquals(emptySet<String>(), result.tags)
        assertNull(result.launchPolicyOverride)
        assertNull(result.themeOverrideId)
        assertNull(result.pinnedOrderOverride)
        assertEquals("", result.notes)
    }

    @Test
    fun `merge with profile applies overrides`() {
        val profile = GameProfile(
            id = "com.example.game",
            artworkOverrides = ArtworkOverrides(hero = "hero", background = "bg"),
            tags = setOf("rpg", "indie"),
            collections = setOf("Favorites"),
            launchPolicyOverride = LaunchPolicyOverride(preferredLogicalRole = SurfaceRole.PRESENTATION),
            themeOverrideId = "dark",
            pinnedOrderOverride = 5,
            notes = "My notes",
            isHidden = true,
            isFavorite = true,
        )
        val result = merge(defaultEntry, profile, resolver)

        assertEquals("file:///artwork/com.example.game/hero.jpg", result.heroArtUrl)
        assertEquals("file:///artwork/com.example.game/bg.jpg", result.backgroundArtUrl)
        assertNull(result.posterArtUrl)
        assertEquals(setOf("rpg", "indie"), result.tags)
        assertEquals(setOf("Favorites"), result.collections)
        assertEquals(SurfaceRole.PRESENTATION, result.launchPolicyOverride?.preferredLogicalRole)
        assertEquals("dark", result.themeOverrideId)
        assertEquals(5, result.pinnedOrderOverride)
        assertEquals("My notes", result.notes)
        assertEquals(true, result.isHidden)
        assertEquals(true, result.isFavorite)
    }

    @Test
    fun `merge artwork fallback when resolver returns null`() {
        val profile = GameProfile(
            id = "com.example.game",
            artworkOverrides = ArtworkOverrides(hero = "missing"),
            tags = emptySet(),
        )
        val nullResolver: (String, String) -> String? = { _, _ -> null }
        val result = merge(defaultEntry, profile, nullResolver)

        assertEquals("pkg:com.example.game", result.heroArtUrl)
        assertEquals("pkg:com.example.game", result.backgroundArtUrl)
    }

    @Test
    fun `merge uses curation when profile collections empty`() {
        val entry = defaultEntry.copy(collections = setOf("Default"))
        val profile = GameProfile(
            id = "com.example.game",
            collections = emptySet(),
            tags = emptySet(),
        )
        val result = merge(entry, profile, resolver)

        assertEquals(setOf("Default"), result.collections)
    }
}
