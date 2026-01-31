package com.launcher.aynthords

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent

class OdinKeyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.MEDIA_BUTTON") {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (event?.keyCode == KeyEvent.KEYCODE_BUTTON_X && event.action == KeyEvent.ACTION_DOWN) {
                ScreenSwapState.toggle()
            }
        }
    }
}
