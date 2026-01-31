package com.launcher.aynthords


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared in-process state: which display should be "white".
 * If true: primary=white, secondary=black.
 * If false: primary=black, secondary=white.
 */
object ScreenSwapState {
    private val _primaryIsWhite = MutableStateFlow(true)
    val primaryIsWhite: StateFlow<Boolean> = _primaryIsWhite.asStateFlow()

    fun toggle() {
        _primaryIsWhite.value = !_primaryIsWhite.value
    }
}