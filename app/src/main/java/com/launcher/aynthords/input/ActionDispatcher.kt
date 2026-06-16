package com.launcher.aynthords.input

import android.view.KeyEvent

/**
 * Helper to resolve keyCode -> Action using current InputConfig.
 * Host activities use this to route hardware input to semantic actions.
 */
object ActionDispatcher {
    fun resolveAction(keyCode: Int): Action? {
        val config = InputConfig.state.value
        return InputMapper.mapKeyToAction(keyCode, config)
    }

    fun resolveNav(keyCode: Int): NavDirection? = InputMapper.mapDpadToNav(keyCode)
}
