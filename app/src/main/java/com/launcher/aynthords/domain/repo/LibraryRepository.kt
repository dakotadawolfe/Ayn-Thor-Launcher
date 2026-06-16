package com.launcher.aynthords.domain.repo

import com.launcher.aynthords.domain.model.LibraryEntry
import kotlinx.coroutines.flow.Flow

enum class LibrarySort {
    Label,
    LastLaunched,
    Custom,
}

interface LibraryRepository {
    val entries: Flow<List<LibraryEntry>>
    fun search(query: String): Flow<List<LibraryEntry>>
    fun setFavorite(packageName: String, favorite: Boolean)
    fun setHidden(packageName: String, hidden: Boolean)
    fun addToCollection(packageName: String, collectionId: String)
    fun removeFromCollection(packageName: String, collectionId: String)
    fun setSortOverride(sort: LibrarySort)
    fun recordLastLaunched(packageName: String)
    fun refresh()
}
