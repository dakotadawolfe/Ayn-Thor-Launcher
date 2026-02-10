package com.launcher.aynthords

import android.app.ActivityOptions
import android.content.Intent
import android.hardware.display.DisplayManager
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.launcher.aynthords.SecondaryHomeActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For now, set the content to a simple white screen
        val view = View(this)
        view.setBackgroundColor(Color.WHITE)
        setContentView(view)

        view.setOnClickListener {
            swapDisplayRoleMappings()
            com.launcher.aynthords.display.DisplayRoleStore.swapScreens(
                source = com.launcher.aynthords.display.ChangeSource.USER_SWAP,
                displayId = display?.displayId,
            )
        }

        // Also launch on create to be more assertive
        launchSecondaryActivityOnSecondaryDisplay()
    }

    override fun onResume() {
        super.onResume()
        // This is the magic. Every time the HomeActivity comes to the foreground,
        // it re-asserts control and ensures the secondary activity is running.
        launchSecondaryActivityOnSecondaryDisplay()
    }

    private fun launchSecondaryActivityOnSecondaryDisplay() {
        val dm = getSystemService(DisplayManager::class.java)
        // Find the first display that is NOT the default one.
        val secondaryDisplay = dm.displays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }

        if (secondaryDisplay != null) {
            val intent = Intent(this, SecondaryHomeActivity::class.java).apply {
                // These flags ensure we start a fresh task on the other screen.
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            val options = ActivityOptions.makeBasic().setLaunchDisplayId(secondaryDisplay.displayId)

            startActivity(intent, options.toBundle())
        }
    }
}