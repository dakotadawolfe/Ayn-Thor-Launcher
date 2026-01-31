package com.launcher.aynthords


import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display

private const val SECONDARY_DISPLAY_ID = 4

fun Activity.ensureSecondaryDisplayActivity() {
    val dm = getSystemService(DisplayManager::class.java) ?: return
    val displays = dm.displays

    // Find a non-default presentation display.
    val secondary = displays.firstOrNull { it.displayId == SECONDARY_DISPLAY_ID } 
        ?: displays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY } 
        ?: return

    val intent = Intent(this, SecondaryHomeActivity::class.java).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
    }


    val opts = ActivityOptions.makeBasic().apply {
        setLaunchDisplayId(secondary.displayId)
    }

    // If it's already running, CLEAR_TOP should bring it forward on that display.
    startActivity(intent, opts.toBundle())
}
