package com.launcher.aynthords.feature.launcher

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.launcher.aynthords.data.local.ArtworkStorage
import com.launcher.aynthords.data.local.GameProfileStore
import com.launcher.aynthords.data.local.LibraryRepositoryImpl
import com.launcher.aynthords.data.local.ProfileRepositoryImpl
import com.launcher.aynthords.domain.model.ResolvedGameModel
import com.launcher.aynthords.domain.repo.LibraryRepository
import com.launcher.aynthords.domain.model.ArtworkOverrides
import com.launcher.aynthords.domain.repo.ProfileRepository
import com.launcher.aynthords.library.LibraryRefreshController
import com.launcher.aynthords.shell.display.PhysicalSurface
import com.launcher.aynthords.shell.launch.LaunchController
import com.launcher.aynthords.shell.launch.LaunchFailure
import com.launcher.aynthords.shell.launch.LaunchResult
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LauncherStore"

// --- State ---
data class LauncherUiState(
    val entries: List<ResolvedGameModel> = emptyList(),
    val focusedEntryId: String? = null,
    val selectedEntryId: String? = null,
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false,
    val activeCollectionId: String? = null,
    val gridColumns: Int = DEFAULT_GRID_COLUMNS,
    /** UI density multiplier (e.g. 0.85f, 1f, 1.15f). Applied to theme spacing/sizes. */
    val uiDensityMultiplier: Float = 1f,
    val launchError: LaunchFailure? = null,
    val launchInProgress: Boolean = false,
    val detailsEntryId: String? = null,
    val settingsOpen: Boolean = false,
    /** When both screens visible: show settings on top (0) or bottom (4) physical display. */
    val settingsDisplayWhenDual: PhysicalSurface = PhysicalSurface.TOP,
) {
    val focusedEntry: ResolvedGameModel? get() = entries.find { it.id == focusedEntryId }
    val selectedEntry: ResolvedGameModel? get() = entries.find { it.id == selectedEntryId }
}

// --- Intents ---
sealed interface LauncherIntent {
    data class SelectEntry(val entryId: String) : LauncherIntent
    data class MoveFocus(val direction: Direction) : LauncherIntent
    object ConfirmSelection : LauncherIntent
    object LaunchSelected : LauncherIntent
    object DismissLaunchError : LauncherIntent
    data class SetSearchQuery(val query: String) : LauncherIntent
    object ToggleFavoritesOnly : LauncherIntent
    data class OpenDetails(val entryId: String) : LauncherIntent
    object CloseDetails : LauncherIntent
    data class ToggleFavorite(val entryId: String) : LauncherIntent
    data class ToggleHidden(val entryId: String) : LauncherIntent
    data class AddToCollection(val entryId: String, val collectionId: String) : LauncherIntent
    data class RemoveFromCollection(val entryId: String, val collectionId: String) : LauncherIntent
    data class SetLaunchRolePreference(val entryId: String, val role: com.launcher.aynthords.domain.model.SurfaceRole) : LauncherIntent
    data class SetThemeOverride(val entryId: String, val themeId: String?) : LauncherIntent
    data class SetGridColumns(val columns: Int) : LauncherIntent
    data class SetUiDensity(val multiplier: Float) : LauncherIntent
    object OpenSettings : LauncherIntent
    object CloseSettings : LauncherIntent
    data class SetSettingsDisplayWhenDual(val surface: PhysicalSurface) : LauncherIntent
}

enum class Direction {
    Up, Down, Left, Right
}

// --- Store (Singleton) ---
object LauncherStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(LauncherUiState())
    private var libraryRepository: LibraryRepository? = null
    private var profileRepository: ProfileRepository? = null

    val state: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private val filterState = MutableStateFlow(FilterState())

    fun initialize(context: Context) {
        if (libraryRepository != null) return
        val appContext = context.applicationContext
        com.launcher.aynthords.input.InputConfig.initialize(appContext)
        val repo = LibraryRepositoryImpl(appContext)
        val profileRepo = ProfileRepositoryImpl(GameProfileStore(appContext))
        libraryRepository = repo
        profileRepository = profileRepo
        LibraryRefreshController.setRepository(repo)

        val filesDir = appContext.filesDir
        val artworkResolver: (String, String) -> String? = { pkg, key ->
            ArtworkStorage.pathFor(filesDir, pkg, ArtworkStorage.keyToType(key))
                .takeIf { it.exists() }
                ?.let { Uri.fromFile(it).toString() }
        }

        scope.launch {
            combine(repo.entries, profileRepo.observeProfiles(), filterState) { entries, profiles, filters ->
                val merged = entries.map { entry -> merge(entry, profiles[entry.id], artworkResolver) }
                applyFilters(merged, filters)
            }.collect { filtered ->
                _uiState.update { current ->
                    val newFocusId = resolveFocusAfterListChange(
                        currentList = filtered,
                        currentFocusId = current.focusedEntryId,
                        currentSelectedId = current.selectedEntryId,
                    )
                    current.copy(
                        entries = filtered,
                        focusedEntryId = newFocusId.first,
                        selectedEntryId = newFocusId.second,
                    )
                }
            }
        }
    }

    private data class FilterState(
        val searchQuery: String = "",
        val showFavoritesOnly: Boolean = false,
        val activeCollectionId: String? = null,
    )

    private fun applyFilters(entries: List<ResolvedGameModel>, filters: FilterState): List<ResolvedGameModel> {
        var list = entries
        if (filters.searchQuery.isNotBlank()) {
            val q = filters.searchQuery.lowercase()
            list = list.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }
        if (filters.showFavoritesOnly) {
            list = list.filter { it.isFavorite }
        }
        if (filters.activeCollectionId != null) {
            list = list.filter { filters.activeCollectionId in it.collections }
        }
        return list
    }

    private fun resolveFocusAfterListChange(
        currentList: List<ResolvedGameModel>,
        currentFocusId: String?,
        currentSelectedId: String?,
    ): Pair<String?, String?> {
        val focusInList = currentFocusId != null && currentList.any { it.id == currentFocusId }
        val selectedInList = currentSelectedId != null && currentList.any { it.id == currentSelectedId }
        return when {
            focusInList -> currentFocusId to (if (selectedInList) currentSelectedId else currentFocusId)
            currentList.isEmpty() -> null to null
            selectedInList -> currentSelectedId!! to currentSelectedId
            else -> {
                val fallbackId = currentList.first().id
                fallbackId to fallbackId
            }
        }
    }

    fun dispatch(intent: LauncherIntent, activity: Activity? = null) {
        when (intent) {
            is LauncherIntent.SelectEntry -> {
                _uiState.update { it.copy(focusedEntryId = intent.entryId, selectedEntryId = intent.entryId) }
            }
            is LauncherIntent.MoveFocus -> {
                handleMoveFocus(intent.direction)
            }
            LauncherIntent.ConfirmSelection -> {
                _uiState.update { state ->
                    state.copy(selectedEntryId = state.focusedEntryId)
                }
            }
            LauncherIntent.LaunchSelected -> {
                val entry = _uiState.value.selectedEntry
                if (entry != null && activity != null && libraryRepository != null) {
                    _uiState.update { it.copy(launchInProgress = true) }
                    val result = LaunchController(activity, libraryRepository!!).launch(entry)
                    _uiState.update { it.copy(launchInProgress = false) }
                    when (result) {
                        is LaunchResult.Success -> Log.d(TAG, "Launched ${entry.label}")
                        is LaunchResult.Failure -> _uiState.update { it.copy(launchError = result.reason) }
                    }
                }
            }
            LauncherIntent.DismissLaunchError -> {
                _uiState.update { it.copy(launchError = null) }
            }
            is LauncherIntent.SetSearchQuery -> {
                filterState.value = filterState.value.copy(searchQuery = intent.query)
                _uiState.update { it.copy(searchQuery = intent.query) }
            }
            LauncherIntent.ToggleFavoritesOnly -> {
                val next = !_uiState.value.showFavoritesOnly
                filterState.value = filterState.value.copy(showFavoritesOnly = next)
                _uiState.update { it.copy(showFavoritesOnly = next) }
            }
            is LauncherIntent.OpenDetails -> {
                _uiState.update { it.copy(detailsEntryId = intent.entryId) }
            }
            LauncherIntent.CloseDetails -> {
                _uiState.update { it.copy(detailsEntryId = null) }
            }
            is LauncherIntent.ToggleFavorite -> {
                val entry = _uiState.value.entries.find { it.id == intent.entryId }
                if (entry != null && libraryRepository != null) {
                    val next = !entry.isFavorite
                    libraryRepository!!.setFavorite(entry.packageName, next)
                    profileRepository?.let { repo ->
                        scope.launch {
                            repo.updateProfile(entry.id) { it.copy(isFavorite = next) }
                        }
                    }
                }
            }
            is LauncherIntent.ToggleHidden -> {
                val entry = _uiState.value.entries.find { it.id == intent.entryId }
                if (entry != null && libraryRepository != null) {
                    val next = !entry.isHidden
                    libraryRepository!!.setHidden(entry.packageName, next)
                    profileRepository?.let { repo ->
                        scope.launch {
                            repo.updateProfile(entry.id) { it.copy(isHidden = next) }
                        }
                    }
                }
            }
            is LauncherIntent.AddToCollection -> {
                val entry = _uiState.value.entries.find { it.id == intent.entryId }
                if (entry != null && libraryRepository != null) {
                    libraryRepository!!.addToCollection(entry.packageName, intent.collectionId)
                    profileRepository?.let { repo ->
                        scope.launch {
                            repo.updateProfile(entry.id) { p ->
                                p.copy(collections = p.collections + intent.collectionId)
                            }
                        }
                    }
                }
            }
            is LauncherIntent.RemoveFromCollection -> {
                val entry = _uiState.value.entries.find { it.id == intent.entryId }
                if (entry != null && libraryRepository != null) {
                    libraryRepository!!.removeFromCollection(entry.packageName, intent.collectionId)
                    profileRepository?.let { repo ->
                        scope.launch {
                            repo.updateProfile(entry.id) { p ->
                                p.copy(collections = p.collections - intent.collectionId)
                            }
                        }
                    }
                }
            }
            is LauncherIntent.SetLaunchRolePreference -> {
                val entry = _uiState.value.entries.find { it.id == intent.entryId }
                if (entry != null) {
                    profileRepository?.let { repo ->
                        scope.launch {
                            repo.updateProfile(entry.id) { p ->
                                p.copy(
                                    launchPolicyOverride = p.launchPolicyOverride?.copy(preferredLogicalRole = intent.role)
                                        ?: com.launcher.aynthords.domain.model.LaunchPolicyOverride(preferredLogicalRole = intent.role)
                                )
                            }
                        }
                    }
                }
            }
            is LauncherIntent.SetThemeOverride -> {
                val entry = _uiState.value.entries.find { it.id == intent.entryId }
                if (entry != null) {
                    profileRepository?.let { repo ->
                        scope.launch {
                            repo.updateProfile(entry.id) { it.copy(themeOverrideId = intent.themeId) }
                        }
                    }
                }
            }
            is LauncherIntent.SetGridColumns -> {
                _uiState.update { it.copy(gridColumns = intent.columns.coerceIn(2, 6)) }
            }
            is LauncherIntent.SetUiDensity -> {
                _uiState.update { it.copy(uiDensityMultiplier = intent.multiplier.coerceIn(0.85f, 1.15f)) }
            }
            LauncherIntent.OpenSettings -> {
                com.launcher.aynthords.feature.settings.SettingsStore.openSettings()
                _uiState.update { it.copy(settingsOpen = true) }
            }
            LauncherIntent.CloseSettings -> {
                _uiState.update { it.copy(settingsOpen = false) }
                com.launcher.aynthords.feature.settings.SettingsStore.reset()
            }
            is LauncherIntent.SetSettingsDisplayWhenDual -> {
                _uiState.update { it.copy(settingsDisplayWhenDual = intent.surface) }
            }
        }
    }

    private fun handleMoveFocus(direction: Direction) {
        val currentState = _uiState.value
        val entries = currentState.entries
        val currentFocusId = currentState.focusedEntryId ?: return
        val currentIndex = entries.indexOfFirst { it.id == currentFocusId }
        if (currentIndex == -1) return

        val nextIndex = gridNavigate(
            currentIndex = currentIndex,
            direction = direction,
            columns = currentState.gridColumns,
            itemCount = entries.size
        ) ?: return

        if (nextIndex != currentIndex) {
            val nextFocusedId = entries[nextIndex].id
            _uiState.update { it.copy(focusedEntryId = nextFocusedId, selectedEntryId = nextFocusedId) }
        }
    }

    /**
     * Apply picked artwork from SAF. Call when OpenDocument returns.
     * Copies file to app storage and updates profile. Never blocks UI.
     */
    fun applyPickedArtwork(
        context: Context,
        gameId: String,
        type: com.launcher.aynthords.data.local.ArtworkType,
        uri: Uri,
    ) {
        val repo = profileRepository ?: return
        val filesDir = context.applicationContext.filesDir
        scope.launch {
            try {
                val dest = ArtworkStorage.pathFor(filesDir, gameId, type)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                ArtworkStorage.evictIfNeeded(filesDir)
                val key = ArtworkStorage.keyFor(type)
                repo.updateProfile(gameId) { p ->
                    p.copy(
                        artworkOverrides = when (type) {
                            com.launcher.aynthords.data.local.ArtworkType.Hero ->
                                p.artworkOverrides.copy(hero = key)
                            com.launcher.aynthords.data.local.ArtworkType.Background ->
                                p.artworkOverrides.copy(background = key)
                            com.launcher.aynthords.data.local.ArtworkType.Poster ->
                                p.artworkOverrides.copy(poster = key)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to apply artwork", e)
            }
        }
    }
}
