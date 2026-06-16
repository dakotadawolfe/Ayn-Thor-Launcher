package com.launcher.aynthords.data.local

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.launcher.aynthords.domain.repo.LibrarySort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.libraryCurationDataStore by preferencesDataStore(name = "library_curation")

data class LibraryCuration(
    val favorites: Set<String> = emptySet(),
    val hidden: Set<String> = emptySet(),
    val sort: LibrarySort = LibrarySort.Label,
    val lastLaunchedAt: Map<String, Long> = emptyMap(),
    val collections: Map<String, Set<String>> = emptyMap(),
)

class LibraryCurationStore(context: Context) {
    private val appContext = context.applicationContext

    private object Keys {
        val favorites = stringSetPreferencesKey("favorites")
        val hidden = stringSetPreferencesKey("hidden")
        val sort = stringPreferencesKey("sort")
        fun lastLaunched(pkg: String) = stringPreferencesKey("last_launched_$pkg")
        fun collection(id: String) = stringSetPreferencesKey("coll_$id")
        val collectionIds = stringSetPreferencesKey("collection_ids")
    }

    val curation: Flow<LibraryCuration> = appContext.libraryCurationDataStore.data.map { prefs ->
        val favorites = prefs[Keys.favorites] ?: emptySet()
        val hidden = prefs[Keys.hidden] ?: emptySet()
        val sortStr = prefs[Keys.sort] ?: LibrarySort.Label.name
        val sort = LibrarySort.entries.find { it.name == sortStr } ?: LibrarySort.Label
        val collectionIds = prefs[Keys.collectionIds] ?: emptySet()
        val collections = collectionIds.associateWith { id ->
            prefs[Keys.collection(id)] ?: emptySet()
        }
        LibraryCuration(
            favorites = favorites,
            hidden = hidden,
            sort = sort,
            lastLaunchedAt = loadLastLaunchedMap(prefs),
            collections = collections,
        )
    }

    private fun loadLastLaunchedMap(prefs: Preferences): Map<String, Long> {
        return prefs.asMap().entries
            .filter { it.key.name.startsWith("last_launched_") }
            .mapNotNull { entry ->
                val name = entry.key.name
                val pkg = name.removePrefix("last_launched_")
                val value = entry.value as? String ?: return@mapNotNull null
                value.toLongOrNull()?.let { pkg to it }
            }
            .toMap()
    }

    fun setFavorite(packageName: String, favorite: Boolean) = runBlocking {
        appContext.libraryCurationDataStore.edit { prefs ->
            val current = prefs[Keys.favorites] ?: emptySet()
            prefs[Keys.favorites] = if (favorite) {
                current + packageName
            } else {
                current - packageName
            }
        }
    }

    fun setHidden(packageName: String, hidden: Boolean) = runBlocking {
        appContext.libraryCurationDataStore.edit { prefs ->
            val current = prefs[Keys.hidden] ?: emptySet()
            prefs[Keys.hidden] = if (hidden) {
                current + packageName
            } else {
                current - packageName
            }
        }
    }

    fun setSort(sort: LibrarySort) = runBlocking {
        appContext.libraryCurationDataStore.edit { prefs ->
            prefs[Keys.sort] = sort.name
        }
    }

    fun addToCollection(packageName: String, collectionId: String) = runBlocking {
        appContext.libraryCurationDataStore.edit { prefs ->
            val ids = prefs[Keys.collectionIds] ?: emptySet()
            prefs[Keys.collectionIds] = ids + collectionId
            val coll = prefs[Keys.collection(collectionId)] ?: emptySet()
            prefs[Keys.collection(collectionId)] = coll + packageName
        }
    }

    fun removeFromCollection(packageName: String, collectionId: String) = runBlocking {
        appContext.libraryCurationDataStore.edit { prefs ->
            val coll = prefs[Keys.collection(collectionId)] ?: emptySet()
            prefs[Keys.collection(collectionId)] = coll - packageName
        }
    }

    fun recordLastLaunched(packageName: String) = runBlocking {
        val now = System.currentTimeMillis()
        appContext.libraryCurationDataStore.edit { prefs ->
            prefs[Keys.lastLaunched(packageName)] = now.toString()
        }
    }
}
