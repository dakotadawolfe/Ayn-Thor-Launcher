package com.launcher.aynthords

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display

private const val PREFERRED_SECONDARY_DISPLAY_ID = 4

class DualScreenSessionController(private val activity: Activity) {

    data class ReassertResult(
        val status: Status,
        val message: String? = null
    )

    enum class Status {
        HEALTHY,
        RECOVERED,
        ERROR
    }

    fun reassert(): ReassertResult {
        val displayManager = activity.getSystemService(DisplayManager::class.java)
            ?: return ReassertResult(Status.ERROR, "Display manager unavailable.")

        val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            ?: return ReassertResult(Status.ERROR, "Primary display is unavailable.")

        val secondaryDisplay = pickSecondaryDisplay(displayManager)
            ?: return ReassertResult(
                Status.ERROR,
                "Secondary display is unavailable. Connect the second screen and retry."
            )

        if (activity.display?.displayId != defaultDisplay.displayId) {
            return ReassertResult(
                Status.ERROR,
                "Primary HOME is on the wrong display. Return HOME to the main screen and retry."
            )
        }

        val secondaryTask = findSecondaryHomeTask()
        val isSecondaryRunningOnExpectedDisplay =
            secondaryTask != null && secondaryTask.taskInfo.displayId == secondaryDisplay.displayId

        if (isSecondaryRunningOnExpectedDisplay) {
            return ReassertResult(Status.HEALTHY)
        }

        return try {
            launchSecondaryOnDisplay(secondaryDisplay.displayId)

            val reason = if (secondaryTask == null) {
                "Secondary HOME was missing and has been relaunched."
            } else {
                "Secondary HOME was on the wrong display and has been moved back."
            }
            ReassertResult(Status.RECOVERED, reason)
        } catch (t: Throwable) {
            ReassertResult(
                Status.ERROR,
                "Unable to launch Secondary HOME on display ${secondaryDisplay.displayId}. ${t.message ?: "Unknown error"}"
            )
        }
    }

    private fun pickSecondaryDisplay(displayManager: DisplayManager): Display? {
        val displays = displayManager.displays
        return displays.firstOrNull { it.displayId == PREFERRED_SECONDARY_DISPLAY_ID }
            ?: displays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }
    }

    private fun findSecondaryHomeTask(): ActivityManager.AppTask? {
        val activityManager = activity.getSystemService(ActivityManager::class.java) ?: return null
        val expectedComponent = ComponentName(activity, SecondaryHomeActivity::class.java)

        return activityManager.appTasks.firstOrNull { appTask ->
            val taskInfo = appTask.taskInfo
            taskInfo.topActivity == expectedComponent || taskInfo.baseActivity == expectedComponent
        }
    }

    private fun launchSecondaryOnDisplay(displayId: Int) {
        val intent = Intent(activity, SecondaryHomeActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        val options = ActivityOptions.makeBasic().apply {
            setLaunchDisplayId(displayId)
        }

        activity.startActivity(intent, options.toBundle())
    }
}
