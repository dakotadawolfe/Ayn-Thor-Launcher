package com.launcher.aynthords

import com.launcher.aynthords.display.DisplayRoleStore

@Deprecated("Use DisplayRoleStore for typed role state")
object ScreenSwapState {
    val state = DisplayRoleStore.state
}
