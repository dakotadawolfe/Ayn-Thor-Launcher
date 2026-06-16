package com.launcher.aynthords.input

import android.view.KeyEvent

/**
 * Represents a physical input (button or key). Used for bindings and glyph lookup.
 */
data class PhysicalInput(val keyCode: Int) {
    val displayName: String get() = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP -> "Dpad Up"
        KeyEvent.KEYCODE_DPAD_DOWN -> "Dpad Down"
        KeyEvent.KEYCODE_DPAD_LEFT -> "Dpad Left"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "Dpad Right"
        KeyEvent.KEYCODE_BUTTON_A -> "A"
        KeyEvent.KEYCODE_BUTTON_B -> "B"
        KeyEvent.KEYCODE_BUTTON_X -> "X"
        KeyEvent.KEYCODE_BUTTON_Y -> "Y"
        KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
        KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
        KeyEvent.KEYCODE_BUTTON_L2 -> "L2"
        KeyEvent.KEYCODE_BUTTON_R2 -> "R2"
        KeyEvent.KEYCODE_BUTTON_START -> "Start"
        KeyEvent.KEYCODE_BUTTON_SELECT -> "Select"
        KeyEvent.KEYCODE_MENU -> "Menu"
        KeyEvent.KEYCODE_ENTER -> "Enter"
        KeyEvent.KEYCODE_BACK -> "Back"
        else -> "Key $keyCode"
    }
}
