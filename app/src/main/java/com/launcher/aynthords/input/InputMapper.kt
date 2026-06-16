package com.launcher.aynthords.input

import android.view.KeyEvent

/**
 * Maps physical key events to semantic Actions using current bindings.
 */
object InputMapper {
    fun mapKeyToAction(keyCode: Int, config: InputConfigState): Action? = config.actionFor(keyCode)

    /** DPAD is always navigation; no remapping. */
    fun mapDpadToNav(keyCode: Int): NavDirection? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP -> NavDirection.Up
        KeyEvent.KEYCODE_DPAD_DOWN -> NavDirection.Down
        KeyEvent.KEYCODE_DPAD_LEFT -> NavDirection.Left
        KeyEvent.KEYCODE_DPAD_RIGHT -> NavDirection.Right
        else -> null
    }
}

enum class NavDirection { Up, Down, Left, Right }
