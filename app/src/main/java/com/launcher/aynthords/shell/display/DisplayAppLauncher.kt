package com.launcher.aynthords.shell.display

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.launcher.aynthords.domain.model.SurfaceRole

object DisplayAppLauncher {
    fun intentForSettings(): Intent =
        Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun intentForYouTube(activity: Activity): Intent {
        // Prefer launching YouTube app if installed
        val pm = activity.packageManager
        val launch = pm.getLaunchIntentForPackage("com.google.android.youtube")
        if (launch != null) return launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Fallback to browser
        return Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun launchOnRole(activity: Activity, intent: Intent, role: SurfaceRole) {
        val displayId = DisplayRoleMappingStore(activity).resolveDisplayId(role) ?: return
        val options = ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
        activity.startActivity(intent, options.toBundle())
    }
}
