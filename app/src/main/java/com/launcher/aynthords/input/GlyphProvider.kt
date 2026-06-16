package com.launcher.aynthords.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders controller glyph for an Action based on current bindings and GlyphSet.
 */
@Composable
fun ActionGlyph(
    action: Action,
    config: InputConfigState,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    backgroundColor: Color,
    textColor: Color,
) {
    val keyCode = config.primaryKeyFor(action)
    val label = when {
        keyCode == null -> "?"
        config.glyphSet == GlyphSet.PlayStation -> keyCodeToPsGlyph(keyCode)
        else -> keyCodeToLabel(keyCode)
    }
    GlyphBox(label = label, modifier = modifier, size = size, backgroundColor = backgroundColor, textColor = textColor)
}

/**
 * Renders a raw glyph by keyCode (for binding display).
 */
@Composable
fun KeyCodeGlyph(
    keyCode: Int,
    glyphSet: GlyphSet,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    backgroundColor: Color,
    textColor: Color,
) {
    val label = when (glyphSet) {
        GlyphSet.PlayStation -> keyCodeToPsGlyph(keyCode)
        else -> keyCodeToLabel(keyCode)
    }
    GlyphBox(label = label, modifier = modifier, size = size, backgroundColor = backgroundColor, textColor = textColor)
}

@Composable
private fun GlyphBox(
    label: String,
    modifier: Modifier,
    size: Dp,
    backgroundColor: Color,
    textColor: Color,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .then(Modifier.size(size)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun keyCodeToLabel(keyCode: Int): String = when (keyCode) {
    android.view.KeyEvent.KEYCODE_DPAD_UP -> "↑"
    android.view.KeyEvent.KEYCODE_DPAD_DOWN -> "↓"
    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> "←"
    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> "→"
    android.view.KeyEvent.KEYCODE_BUTTON_A -> "A"
    android.view.KeyEvent.KEYCODE_BUTTON_B -> "B"
    android.view.KeyEvent.KEYCODE_BUTTON_X -> "X"
    android.view.KeyEvent.KEYCODE_BUTTON_Y -> "Y"
    android.view.KeyEvent.KEYCODE_BUTTON_L1 -> "L1"
    android.view.KeyEvent.KEYCODE_BUTTON_R1 -> "R1"
    android.view.KeyEvent.KEYCODE_BUTTON_START -> "Start"
    android.view.KeyEvent.KEYCODE_BUTTON_SELECT -> "Select"
    android.view.KeyEvent.KEYCODE_MENU -> "⋮"
    android.view.KeyEvent.KEYCODE_ENTER -> "↵"
    android.view.KeyEvent.KEYCODE_BACK -> "←"
    else -> "?"
}

private fun keyCodeToPsGlyph(keyCode: Int): String = when (keyCode) {
    android.view.KeyEvent.KEYCODE_BUTTON_A -> "×"  // PS X (bottom)
    android.view.KeyEvent.KEYCODE_BUTTON_B -> "○"  // PS Circle (right)
    android.view.KeyEvent.KEYCODE_BUTTON_X -> "□"  // PS Square (left)
    android.view.KeyEvent.KEYCODE_BUTTON_Y -> "△"  // PS Triangle (top)
    else -> keyCodeToLabel(keyCode)
}