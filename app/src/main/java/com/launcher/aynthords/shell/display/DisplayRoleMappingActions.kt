package com.launcher.aynthords.shell.display

import android.app.Activity
import android.content.Context
import com.launcher.aynthords.DebugLog
import com.launcher.aynthords.domain.model.SurfaceRole

/**
 * Swap presentation and interaction screen roles and persist to DisplayRoleMappingStore.
 * Callable from host activities (with displayId) or OdinKeyReceiver (with null).
 */
fun performUserSwap(context: Context, displayId: Int?) {
    DisplayRoleStore.swapScreens(ChangeSource.USER_SWAP, displayId)
    val state = DisplayRoleStore.state.value
    val mapping = state.currentMapping
    // #region agent log
    val reason = (state as? DisplayRoleState.Rejected)?.reason
    DebugLog.log(context, "H5", "performUserSwap", "after swapScreens", "stateClass" to state::class.simpleName, "top" to mapping.top.name, "bottom" to mapping.bottom.name, "rejectReason" to reason)
    // #endregion
    val mappingStore = DisplayRoleMappingStore(context.applicationContext)
    mappingStore.setDisplayId(SurfaceRole.INTERACTION, DisplayRoleStore.displayIdForRole(mapping, SurfaceRole.INTERACTION))
    mappingStore.setDisplayId(SurfaceRole.PRESENTATION, DisplayRoleStore.displayIdForRole(mapping, SurfaceRole.PRESENTATION))
}

fun Activity.swapDisplayRoleMappings() {
    val store = DisplayRoleMappingStore(this)
    val interactionId = store.resolveDisplayId(SurfaceRole.INTERACTION) ?: return
    val presentationId = store.resolveDisplayId(SurfaceRole.PRESENTATION) ?: return

    if (interactionId == presentationId) return

    store.setDisplayId(SurfaceRole.INTERACTION, presentationId)
    store.setDisplayId(SurfaceRole.PRESENTATION, interactionId)
}
