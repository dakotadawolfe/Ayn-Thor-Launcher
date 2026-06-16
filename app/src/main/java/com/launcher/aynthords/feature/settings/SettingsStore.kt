package com.launcher.aynthords.feature.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val selectedCategoryId: String? = SettingsCategories.all.first().id,
    val selectedSubpageId: String? = null,
    val searchQuery: String = "",
    val focusedCategoryIndex: Int = 0,
    val focusedItemIndex: Int = 0,
    val focusInItems: Boolean = true,
)

object SettingsStore {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun selectCategory(id: String?) {
        _state.update {
            val idx = if (id != null) SettingsCategories.all.indexOfFirst { c -> c.id == id }.coerceAtLeast(0) else it.focusedCategoryIndex
            it.copy(
                selectedCategoryId = id,
                selectedSubpageId = null,
                focusedCategoryIndex = idx,
                focusedItemIndex = 0,
                focusInItems = id != null,
            )
        }
    }

    /** Called when opening settings: select first category with focus on rail. */
    fun openSettings() {
        val firstId = SettingsCategories.all.first().id
        val idx = SettingsCategories.all.indexOfFirst { c -> c.id == firstId }.coerceAtLeast(0)
        _state.value = SettingsUiState(
            selectedCategoryId = firstId,
            selectedSubpageId = null,
            searchQuery = "",
            focusedCategoryIndex = idx,
            focusedItemIndex = 0,
            focusInItems = false,
        )
    }

    fun handleDpad(direction: DpadDirection, isDefaultHome: Boolean = true): Boolean {
        val current = _state.value
        return when {
            current.selectedSubpageId == "diagnostics" -> {
                if (direction == DpadDirection.Left || direction == DpadDirection.Back) {
                    selectSubpage(null)
                    true
                } else false
            }
            current.selectedSubpageId == "customizeControls" -> {
                if (direction == DpadDirection.Left || direction == DpadDirection.Back) {
                    selectSubpage("controllerLayout")
                    true
                } else false
            }
            current.selectedSubpageId == "controllerLayout" -> {
                if (direction == DpadDirection.Left || direction == DpadDirection.Back) {
                    selectSubpage(null)
                    true
                } else false
            }
            !current.focusInItems -> {
                val idx = when (direction) {
                    DpadDirection.Up -> (current.focusedCategoryIndex - 1).coerceIn(0, SettingsCategories.all.lastIndex)
                    DpadDirection.Down -> (current.focusedCategoryIndex + 1).coerceIn(0, SettingsCategories.all.lastIndex)
                    DpadDirection.Right -> {
                        val cat = SettingsCategories.all.getOrNull(current.focusedCategoryIndex)
                        if (cat != null) {
                            selectCategory(cat.id)
                        }
                        return true
                    }
                    else -> return false
                }
                _state.update { it.copy(focusedCategoryIndex = idx) }
                true
            }
            else -> {
                val category = SettingsCategories.categoryById(current.selectedCategoryId ?: "") ?: return false
                val itemCount = category.items.size + (if (category.id == "system" && !isDefaultHome) 1 else 0)
                val idx = when (direction) {
                    DpadDirection.Up -> (current.focusedItemIndex - 1).coerceIn(0, (itemCount - 1).coerceAtLeast(0))
                    DpadDirection.Down -> (current.focusedItemIndex + 1).coerceIn(0, (itemCount - 1).coerceAtLeast(0))
                    DpadDirection.Left -> {
                        focusBackToRail()
                        return true
                    }
                    else -> return false
                }
                _state.update { it.copy(focusedItemIndex = idx) }
                true
            }
        }
    }

    enum class DpadDirection { Up, Down, Left, Right, Back }

    fun selectSubpage(id: String?) {
        _state.update {
            it.copy(selectedSubpageId = id)
        }
    }

    fun setSearchQuery(query: String) {
        _state.update {
            it.copy(searchQuery = query)
        }
    }

    /** Move focus back one level. Returns true if consumed (caller should not close settings). */
    fun handleBack(): Boolean {
        val current = _state.value
        return when {
            current.selectedSubpageId == "customizeControls" -> {
                selectSubpage("controllerLayout")
                true
            }
            current.selectedSubpageId == "controllerLayout" || current.selectedSubpageId == "diagnostics" -> {
                selectSubpage(null)
                true
            }
            current.focusInItems -> {
                focusBackToRail()
                true
            }
            else -> false // On rail at main menu: caller should close settings
        }
    }

    fun focusOnRail() {
        _state.update {
            val idx = it.selectedCategoryId?.let { id ->
                SettingsCategories.all.indexOfFirst { c -> c.id == id }.coerceAtLeast(0)
            } ?: 0
            it.copy(focusInItems = false, focusedCategoryIndex = idx)
        }
    }

    private fun focusBackToRail() = focusOnRail()

    fun resetCategory() {
        _state.update {
            it.copy(selectedSubpageId = null)
        }
    }

    fun reset() {
        _state.value = SettingsUiState()
    }

    fun getFocusedItemAction(isDefaultHome: Boolean): Pair<String, String>? {
        val current = _state.value
        val category = SettingsCategories.categoryById(current.selectedCategoryId ?: return null) ?: return null
        val items = category.items.toMutableList()
        if (category.id == "system" && !isDefaultHome) {
            items.add(SettingsItem("setDefaultLauncher", "Set as default launcher", "HOME will open Ayn Thor Launcher", null))
        }
        val item = items.getOrNull(current.focusedItemIndex) ?: return null
        return category.id to item.id
    }

    fun getFocusedCategoryForActivation(): String? {
        val current = _state.value
        return if (!current.focusInItems) {
            SettingsCategories.all.getOrNull(current.focusedCategoryIndex)?.id
        } else null
    }
}
