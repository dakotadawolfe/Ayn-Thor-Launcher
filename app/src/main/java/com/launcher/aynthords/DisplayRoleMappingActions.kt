package com.launcher.aynthords

import android.app.Activity

fun Activity.swapDisplayRoleMappings() {
    val store = DisplayRoleMappingStore(this)
    val interactionId = store.resolveDisplayId(DisplayRole.INTERACTION) ?: return
    val presentationId = store.resolveDisplayId(DisplayRole.PRESENTATION) ?: return

    if (interactionId == presentationId) return

    store.setDisplayId(DisplayRole.INTERACTION, presentationId)
    store.setDisplayId(DisplayRole.PRESENTATION, interactionId)
}
