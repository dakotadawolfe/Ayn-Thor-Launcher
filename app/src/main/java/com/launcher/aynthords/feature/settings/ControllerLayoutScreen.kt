package com.launcher.aynthords.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.launcher.aynthords.input.Action
import com.launcher.aynthords.input.GlyphSet
import com.launcher.aynthords.input.InputConfig
import com.launcher.aynthords.input.InputPreset
import com.launcher.aynthords.input.KeyCodeGlyph
import com.launcher.aynthords.theme.runtime.LocalThemeRuntime
import kotlinx.coroutines.launch

@Composable
fun ControllerLayoutContent(
    onDismiss: () -> Unit,
    onOpenCustomize: () -> Unit,
) {
    val theme = LocalThemeRuntime.current
    val scope = rememberCoroutineScope()
    val config by InputConfig.state.collectAsState()
    val store = InputConfig.getStore(androidx.compose.ui.platform.LocalContext.current)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = "Controller Layout",
            color = theme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text("Preset", color = theme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            InputPreset.entries.forEach { preset ->
                val isSelected = config.preset == preset
                Row(
                    modifier = Modifier
                        .clickable { scope.launch { store.setPreset(preset) } }
                        .background(
                            if (isSelected) theme.surfaceVariant.copy(alpha = 0.2f) else Color.Transparent,
                            androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = preset.name,
                        color = if (isSelected) theme.onSurface else theme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
            }
        }

        Text("Glyph style", color = theme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            GlyphSet.entries.forEach { gs ->
                val isSelected = config.glyphSet == gs
                Row(
                    modifier = Modifier
                        .clickable { scope.launch { store.setGlyphSet(gs) } }
                        .background(
                            if (isSelected) theme.surfaceVariant.copy(alpha = 0.2f) else Color.Transparent,
                            androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = gs.name,
                        color = if (isSelected) theme.onSurface else theme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { scope.launch { store.setSwapConfirmBack(!config.swapConfirmBack) } }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Swap Confirm/Back (for PlayStation X/O)", color = theme.onSurface, fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (config.swapConfirmBack) "On" else "Off",
                color = theme.onSurfaceVariant,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { scope.launch { store.resetToPreset() } }
                .background(theme.surfaceVariant.copy(alpha = 0.15f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .padding(12.dp),
        ) {
            Text("Reset to preset", color = theme.onSurface, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenCustomize)
                .background(theme.surfaceVariant.copy(alpha = 0.15f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .padding(12.dp),
        ) {
            Text("Customize bindings…", color = theme.onSurface, fontSize = 14.sp)
        }
    }
}

@Composable
fun CustomizeControlsContent(
    onDismiss: () -> Unit,
) {
    val theme = LocalThemeRuntime.current
    val config by InputConfig.state.collectAsState()
    val rebindingAction by InputConfig.rebindingAction.collectAsState()
    val store = InputConfig.getStore(androidx.compose.ui.platform.LocalContext.current)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = "Customize Controls",
            color = theme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        val generalActions = listOf(Action.Confirm, Action.Back, Action.Menu, Action.Search, Action.Context)
        val navActions = listOf(Action.NavUp, Action.NavDown, Action.NavLeft, Action.NavRight)
        val otherActions = listOf(Action.Details, Action.ResetCategory, Action.QuickSwapDisplays)

        Text("General", color = theme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp, top = 8.dp))
        generalActions.forEach { action ->
            ControlRow(
                action = action,
                config = config,
                theme = theme,
                isRebinding = rebindingAction == action,
                onStartRebind = { InputConfig.setRebindingAction(action) },
                onClearRebind = { InputConfig.setRebindingAction(null) },
            )
        }

        Text("Navigation", color = theme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp, top = 16.dp))
        navActions.forEach { action ->
            ControlRow(
                action = action,
                config = config,
                theme = theme,
                isRebinding = rebindingAction == action,
                onStartRebind = { InputConfig.setRebindingAction(action) },
                onClearRebind = { InputConfig.setRebindingAction(null) },
            )
        }

        Text("Other", color = theme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp, top = 16.dp))
        otherActions.forEach { action ->
            ControlRow(
                action = action,
                config = config,
                theme = theme,
                isRebinding = rebindingAction == action,
                onStartRebind = { InputConfig.setRebindingAction(action) },
                onClearRebind = { InputConfig.setRebindingAction(null) },
            )
        }

        if (rebindingAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Press a button for ${rebindingAction!!.name}…",
                color = theme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ControlRow(
    action: Action,
    config: com.launcher.aynthords.input.InputConfigState,
    theme: com.launcher.aynthords.theme.runtime.ThemeRuntime,
    isRebinding: Boolean,
    onStartRebind: () -> Unit,
    onClearRebind: () -> Unit,
) {
    val keyCode = config.primaryKeyFor(action)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = action.name.replaceFirstChar { it.uppercase() },
            color = theme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        if (keyCode != null) {
            KeyCodeGlyph(
                keyCode = keyCode,
                glyphSet = config.glyphSet,
                size = 24.dp,
                backgroundColor = theme.surfaceVariant.copy(alpha = 0.4f),
                textColor = theme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = if (isRebinding) "Cancel" else "Rebind",
            color = theme.primary,
            fontSize = 12.sp,
            modifier = Modifier.clickable {
                if (isRebinding) onClearRebind() else onStartRebind()
            },
        )
    }
}
