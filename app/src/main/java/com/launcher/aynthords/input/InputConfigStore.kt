package com.launcher.aynthords.input

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.inputConfigDataStore by preferencesDataStore(name = "input_config")

private const val SCHEMA_VERSION = 1

data class InputConfigState(
    val schemaVersion: Int = SCHEMA_VERSION,
    val glyphSet: GlyphSet = GlyphSet.Nintendo,
    val preset: InputPreset = InputPreset.Nintendo,
    val customBindings: Map<Action, Set<Int>> = emptyMap(),
    val swapConfirmBack: Boolean = false,
) {
    /** Resolved bindings: custom overrides + preset defaults for unbound actions */
    fun bindingsFor(action: Action): Set<Int> {
        val custom = customBindings[action]
        if (custom != null && custom.isNotEmpty()) return custom
        return InputPresets.forPreset(preset)[action] ?: emptySet()
    }

    fun actionFor(keyCode: Int): Action? {
        val raw = Action.entries.firstOrNull { bindingsFor(it).contains(keyCode) } ?: return null
        if (swapConfirmBack && (raw == Action.Confirm || raw == Action.Back)) {
            return if (raw == Action.Confirm) Action.Back else Action.Confirm
        }
        return raw
    }

    /** Primary keyCode for an action (for glyph display). Accounts for swapConfirmBack. */
    fun primaryKeyFor(action: Action): Int? {
        val resolved = if (swapConfirmBack && (action == Action.Confirm || action == Action.Back)) {
            if (action == Action.Confirm) Action.Back else Action.Confirm
        } else action
        return bindingsFor(resolved).firstOrNull()
    }
}

class InputConfigStore(context: Context) {
    private val appContext = context.applicationContext

    private object Keys {
        val glyphSet = stringPreferencesKey("glyph_set")
        val preset = stringPreferencesKey("preset")
        fun actionBinding(action: Action) = stringSetPreferencesKey("binding_${action.name}")
        val swapConfirmBack = stringPreferencesKey("swap_confirm_back")
    }

    val state: Flow<InputConfigState> = appContext.inputConfigDataStore.data.map { prefs ->
        InputConfigState(
            schemaVersion = SCHEMA_VERSION,
            glyphSet = prefs[Keys.glyphSet]?.let { GlyphSet.valueOf(it) } ?: GlyphSet.Nintendo,
            preset = prefs[Keys.preset]?.let { InputPreset.valueOf(it) } ?: InputPreset.Nintendo,
            customBindings = Action.entries.associateWith { action ->
                (prefs[Keys.actionBinding(action)] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
            }.filterValues { it.isNotEmpty() },
            swapConfirmBack = prefs[Keys.swapConfirmBack] == "1",
        )
    }

    fun stateBlocking(): InputConfigState = runBlocking { state.first() }

    suspend fun setGlyphSet(glyphSet: GlyphSet) {
        appContext.inputConfigDataStore.edit { it[Keys.glyphSet] = glyphSet.name }
    }

    suspend fun setPreset(preset: InputPreset) {
        appContext.inputConfigDataStore.edit {
            it[Keys.preset] = preset.name
            Action.entries.forEach { action -> it.remove(Keys.actionBinding(action)) }
        }
    }

    suspend fun setBinding(action: Action, keyCodes: Set<Int>) {
        appContext.inputConfigDataStore.edit {
            if (keyCodes.isEmpty()) {
                it.remove(Keys.actionBinding(action))
            } else {
                it[Keys.actionBinding(action)] = keyCodes.map { it.toString() }.toSet()
            }
        }
    }

    suspend fun setSwapConfirmBack(swap: Boolean) {
        appContext.inputConfigDataStore.edit {
            it[Keys.swapConfirmBack] = if (swap) "1" else "0"
        }
    }

    suspend fun resetToPreset() {
        appContext.inputConfigDataStore.edit { prefs: MutablePreferences ->
            Action.entries.forEach { prefs.remove(Keys.actionBinding(it)) }
        }
    }
}
