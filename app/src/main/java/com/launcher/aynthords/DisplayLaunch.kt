package com.launcher.aynthords

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent

fun Activity.ensureSecondaryDisplayActivity() {
    val presentationDisplayId = DisplayRoleMappingStore(this)
        .resolveDisplayId(DisplayRole.PRESENTATION)
        ?: return

    val intent = Intent(this, SecondaryHomeActivity::class.java).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
    }

    val opts = ActivityOptions.makeBasic().apply {
        setLaunchDisplayId(presentationDisplayId)
    }

    // If it's already running, CLEAR_TOP should bring it forward on that display.
    startActivity(intent, opts.toBundle())
}
