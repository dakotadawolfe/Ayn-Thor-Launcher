package com.launcher.aynthords.data.local

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.launcher.aynthords.domain.model.LibraryEntry
import com.launcher.aynthords.domain.repo.LibraryRepository
import com.launcher.aynthords.domain.repo.LibrarySort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow

private const val PKG_SCHEME = "pkg:"

class LibraryRepositoryImpl(context: Context) : LibraryRepository {
    private val appContext = context.applicationContext
    private val pm: PackageManager = appContext.packageManager
    private val curationStore = LibraryCurationStore(appContext)
    private val launcherPackage = appContext.packageName

    private val refreshTrigger = MutableStateFlow(0)

    override val entries: Flow<List<LibraryEntry>> = combine(
        curationStore.curation,
        refreshTrigger
    ) { curation, _ ->
        buildEntries(curation)
    }.distinctUntilChanged()

    override fun search(query: String): Flow<List<LibraryEntry>> =
        if (query.isBlank()) entries else entries.map { list ->
            val q = query.lowercase()
            list.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }

    override fun setFavorite(packageName: String, favorite: Boolean) {
        curationStore.setFavorite(packageName, favorite)
    }

    override fun setHidden(packageName: String, hidden: Boolean) {
        curationStore.setHidden(packageName, hidden)
    }

    override fun addToCollection(packageName: String, collectionId: String) {
        curationStore.addToCollection(packageName, collectionId)
    }

    override fun removeFromCollection(packageName: String, collectionId: String) {
        curationStore.removeFromCollection(packageName, collectionId)
    }

    override fun setSortOverride(sort: LibrarySort) {
        curationStore.setSort(sort)
    }

    override fun recordLastLaunched(packageName: String) {
        curationStore.recordLastLaunched(packageName)
    }

    override fun refresh() {
        refreshTrigger.value += 1
    }

    private fun buildEntries(curation: LibraryCuration): List<LibraryEntry> {
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            ?: return emptyList()

        val entries = apps
            .filter { it.packageName != launcherPackage }
            .filter { it.packageName !in curation.hidden }
            .map { info ->
                val label = info.loadLabel(pm)?.toString() ?: info.packageName
                val isGame = isGameHeuristic(info)
                val isFavorite = info.packageName in curation.favorites
                val collections = curation.collections
                    .filter { (_, pkgs) -> info.packageName in pkgs }
                    .keys
                    .toSet()
                val lastLaunchedAt = curation.lastLaunchedAt[info.packageName]

                LibraryEntry(
                    id = info.packageName,
                    packageName = info.packageName,
                    label = label,
                    iconCacheKey = "$PKG_SCHEME${info.packageName}",
                    isGame = isGame,
                    isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isHidden = false,
                    isFavorite = isFavorite,
                    collections = collections,
                    lastLaunchedAt = lastLaunchedAt,
                )
            }

        return sortEntries(entries, curation.sort)
    }

    private fun isGameHeuristic(info: ApplicationInfo): Boolean {
        val pkg = info.packageName.lowercase()
        val patterns = listOf(".game", ".games", "com.game", "emu", "emulator", "retro", "rom")
        return patterns.any { pkg.contains(it) }
    }

    private fun sortEntries(entries: List<LibraryEntry>, sort: LibrarySort): List<LibraryEntry> {
        return when (sort) {
            LibrarySort.Label -> entries.sortedWith(
                compareBy<LibraryEntry> { !it.isFavorite }
                    .thenBy { it.label.lowercase() }
            )
            LibrarySort.LastLaunched -> entries.sortedWith(
                compareBy<LibraryEntry> { !it.isFavorite }
                    .thenByDescending { it.lastLaunchedAt ?: 0L }
                    .thenBy { it.label.lowercase() }
            )
            LibrarySort.Custom -> entries.sortedWith(
                compareBy<LibraryEntry> { !it.isFavorite }
                    .thenBy { it.label.lowercase() }
            )
        }
    }
}
