package com.launcher.aynthords.shell

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.os.Build

/**
 * Helper to check and request the default Home launcher role.
 * Required for the app to launch automatically when pressing HOME (e.g. after force stop).
 */
object HomeRoleHelper {

    /**
     * True if this app is the default Home launcher.
     * When false, pressing HOME may show Launcher3 (QuickStep) or a chooser.
     */
    fun isDefaultHome(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 29) return false
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return false
        return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
    }

    /**
     * Launch the system UI to let the user set this app as the default Home launcher.
     * Call from an Activity (startActivityForResult / ActivityResultLauncher).
     * No-op if already default or RoleManager unavailable.
     */
    fun launchRequestHomeRole(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < 29) return false
        val roleManager = activity.getSystemService(RoleManager::class.java) ?: return false
        if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return false
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) return false
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME) ?: return false
        activity.startActivity(intent)
        return true
    }
}
