package com.launcher.aynthords.input

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Global input config. Initialized with LauncherStore.
 */
object InputConfig {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var store: InputConfigStore? = null
    private val _state = MutableStateFlow(InputConfigState())
    val state: StateFlow<InputConfigState> = _state.asStateFlow()

    private val _rebindingAction = MutableStateFlow<Action?>(null)
    val rebindingAction: StateFlow<Action?> = _rebindingAction.asStateFlow()

    fun initialize(context: Context) {
        if (store != null) return
        store = InputConfigStore(context.applicationContext)
        scope.launch {
            store!!.state.collect { _state.value = it }
        }
    }

    fun getStore(context: Context): InputConfigStore {
        if (store == null) initialize(context.applicationContext)
        return store!!
    }

    fun setRebindingAction(action: Action?) {
        _rebindingAction.value = action
    }

    /**
     * Call from activity when a key is pressed during rebinding.
     * Returns true if the key was captured (consumed).
     */
    fun captureRebindingKey(context: Context, keyCode: Int): Boolean {
        val action = _rebindingAction.value ?: return false
        _rebindingAction.value = null

        when (keyCode) {
            android.view.KeyEvent.KEYCODE_DPAD_UP,
            android.view.KeyEvent.KEYCODE_DPAD_DOWN,
            android.view.KeyEvent.KEYCODE_DPAD_LEFT,
            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> return true // Consume but cancel (don't bind DPAD)
            android.view.KeyEvent.KEYCODE_BACK -> return true // Cancel rebinding
            else -> {
                scope.launch {
                    getStore(context.applicationContext).setBinding(action, setOf(keyCode))
                }
                return true
            }
        }
    }
}
