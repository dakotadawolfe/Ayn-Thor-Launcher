package com.launcher.aynthords.input

import android.view.KeyEvent

/**
 * Preset bindings. User can override any action.
 */
object InputPresets {
    val nintendo: Map<Action, Set<Int>> = mapOf(
        Action.Confirm to setOf(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER),
        Action.Back to setOf(KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK),
        Action.Menu to setOf(KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU),
        Action.Search to setOf(KeyEvent.KEYCODE_BUTTON_Y),
        Action.Context to setOf(KeyEvent.KEYCODE_BUTTON_Y),
        Action.Details to setOf(KeyEvent.KEYCODE_BUTTON_Y),
        Action.ResetCategory to setOf(KeyEvent.KEYCODE_BUTTON_Y),
        Action.QuickSwapDisplays to setOf(KeyEvent.KEYCODE_BUTTON_X),
        Action.NavUp to setOf(KeyEvent.KEYCODE_DPAD_UP),
        Action.NavDown to setOf(KeyEvent.KEYCODE_DPAD_DOWN),
        Action.NavLeft to setOf(KeyEvent.KEYCODE_DPAD_LEFT),
        Action.NavRight to setOf(KeyEvent.KEYCODE_DPAD_RIGHT),
    )

    val xbox: Map<Action, Set<Int>> = mapOf(
        Action.Confirm to setOf(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER),
        Action.Back to setOf(KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK),
        Action.Menu to setOf(KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU),
        Action.Search to setOf(KeyEvent.KEYCODE_BUTTON_X),
        Action.Context to setOf(KeyEvent.KEYCODE_BUTTON_X),
        Action.Details to setOf(KeyEvent.KEYCODE_BUTTON_X),
        Action.ResetCategory to setOf(KeyEvent.KEYCODE_BUTTON_X),
        Action.QuickSwapDisplays to setOf(KeyEvent.KEYCODE_BUTTON_SELECT),
        Action.NavUp to setOf(KeyEvent.KEYCODE_DPAD_UP),
        Action.NavDown to setOf(KeyEvent.KEYCODE_DPAD_DOWN),
        Action.NavLeft to setOf(KeyEvent.KEYCODE_DPAD_LEFT),
        Action.NavRight to setOf(KeyEvent.KEYCODE_DPAD_RIGHT),
    )

    /** PlayStation: Confirm=X, Back=Circle, Menu=Triangle, Context=Square */
    val playStation: Map<Action, Set<Int>> = mapOf(
        Action.Confirm to setOf(KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_ENTER),
        Action.Back to setOf(KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK),
        Action.Menu to setOf(KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_MENU),
        Action.Search to setOf(KeyEvent.KEYCODE_BUTTON_A),
        Action.Context to setOf(KeyEvent.KEYCODE_BUTTON_A),
        Action.Details to setOf(KeyEvent.KEYCODE_BUTTON_A),
        Action.ResetCategory to setOf(KeyEvent.KEYCODE_BUTTON_A),
        Action.QuickSwapDisplays to setOf(KeyEvent.KEYCODE_BUTTON_A), // Square—A maps to Square on PS
        Action.NavUp to setOf(KeyEvent.KEYCODE_DPAD_UP),
        Action.NavDown to setOf(KeyEvent.KEYCODE_DPAD_DOWN),
        Action.NavLeft to setOf(KeyEvent.KEYCODE_DPAD_LEFT),
        Action.NavRight to setOf(KeyEvent.KEYCODE_DPAD_RIGHT),
    )

    fun forPreset(preset: InputPreset): Map<Action, Set<Int>> = when (preset) {
        InputPreset.Nintendo -> nintendo
        InputPreset.Xbox -> xbox
        InputPreset.PlayStation -> playStation
    }
}

enum class InputPreset {
    Nintendo,
    Xbox,
    PlayStation,
}
