package com.launcher.aynthords

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import com.launcher.aynthords.input.Action
import com.launcher.aynthords.input.InputConfig
import com.launcher.aynthords.input.InputMapper
import com.launcher.aynthords.shell.display.performUserSwap

class OdinKeyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.MEDIA_BUTTON") {
            val event: KeyEvent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }
            if (event?.action == KeyEvent.ACTION_DOWN) {
                val config = InputConfig.getStore(context.applicationContext).stateBlocking()
                if (InputMapper.mapKeyToAction(event.keyCode, config) == Action.QuickSwapDisplays) {
                    performUserSwap(context.applicationContext, null)
                }
            }
        }
    }
}
