package com.launcher.aynthords.library

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives PACKAGE_ADDED, PACKAGE_REMOVED, PACKAGE_CHANGED and triggers
 * debounced library refresh via [LibraryRefreshController].
 */
class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_CHANGED -> {
                LibraryRefreshController.requestRefresh()
            }
        }
    }

}
