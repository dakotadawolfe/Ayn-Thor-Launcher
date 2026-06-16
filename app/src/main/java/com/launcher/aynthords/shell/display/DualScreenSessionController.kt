package com.launcher.aynthords.shell.display

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.os.Build
import androidx.core.content.ContextCompat
import com.launcher.aynthords.domain.model.SessionStatus
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.InteractionHostActivity
import com.launcher.aynthords.PresentationHostActivity
import com.launcher.aynthords.shell.display.ensureInteractionOnDisplay
import com.launcher.aynthords.shell.display.ensureSecondaryDisplayActivity
import com.launcher.aynthords.DebugLog

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

    /**
     * Ensure both hosts are on their correct displays.
     * Called from onResume (HOME) to launch Presentation when returning to launcher.
     */
    fun reassert(): ReassertResult {
        val currentDisplayId = ContextCompat.getDisplayOrDefault(activity).displayId
        val sessionController = DisplaySessionController(activity)
        val sessionResult = sessionController.reassert(currentDisplayId)
        val state = sessionResult.state

        return when (sessionResult.status) {
            SessionStatus.SINGLE_SCREEN -> {
                // Single-display fallback: presentation remains dormant but interaction is healthy.
                ReassertResult(Status.HEALTHY, null)
            }
            SessionStatus.DUAL_SCREEN -> {
                val interactionAction = sessionResult.actions
                    .filterIsInstance<DisplaySessionController.ReassertAction.LaunchInteractionOnDisplay>()
                    .firstOrNull()
                val presentationAction = sessionResult.actions
                    .filterIsInstance<DisplaySessionController.ReassertAction.LaunchPresentationOnDisplay>()
                    .firstOrNull()

                val interactionDisplayId = interactionAction?.displayId
                val presentationDisplayId = presentationAction?.displayId

                val interactionOnDisplay = interactionDisplayId != null && isInteractionOnDisplay(interactionDisplayId)
                val presentationOnDisplay = presentationDisplayId != null && isPresentationOnDisplay(presentationDisplayId)
                val interactionNeedsLaunch = interactionDisplayId != null && !interactionOnDisplay
                val presentationNeedsLaunch = presentationDisplayId != null && !presentationOnDisplay

                // #region agent log
                DebugLog.log(activity.applicationContext, "H1", "DualScreenSessionController.reassert", "DUAL_SCREEN check",
                    "sessionStatus" to sessionResult.status.name,
                    "interactionOnDisplay" to interactionOnDisplay,
                    "presentationOnDisplay" to presentationOnDisplay,
                    "interactionNeedsLaunch" to interactionNeedsLaunch,
                    "presentationNeedsLaunch" to presentationNeedsLaunch,
                    "interactionDisplayId" to interactionDisplayId,
                    "presentationDisplayId" to presentationDisplayId)
                // #endregion

                if (!interactionNeedsLaunch && !presentationNeedsLaunch) {
                    return ReassertResult(Status.HEALTHY, null)
                }

                try {
                    if (interactionNeedsLaunch) {
                        // #region agent log
                        DebugLog.log(activity.applicationContext, "H2", "DualScreenSessionController.reassert", "calling ensureInteractionOnDisplay",
                            "displayId" to interactionDisplayId)
                        // #endregion
                        activity.ensureInteractionOnDisplay(interactionDisplayId!!)
                    }
                    if (presentationNeedsLaunch) {
                        // #region agent log
                        DebugLog.log(activity.applicationContext, "H2", "DualScreenSessionController.reassert", "calling ensureSecondaryDisplayActivity",
                            "displayId" to presentationDisplayId)
                        // #endregion
                        activity.ensureSecondaryDisplayActivity(presentationDisplayId!!)
                    }
                    val reason = when {
                        interactionNeedsLaunch && presentationNeedsLaunch ->
                            "Both hosts were on wrong displays and have been moved."
                        interactionNeedsLaunch ->
                            "Interaction host was on wrong display and has been moved."
                        else ->
                            "Presentation host was on wrong display and has been moved."
                    }
                    ReassertResult(Status.RECOVERED, reason)
                } catch (t: Throwable) {
                    ReassertResult(
                        Status.ERROR,
                        "Unable to launch hosts. ${t.message ?: "Unknown error"}"
                    )
                }
            }
            SessionStatus.DEGRADED -> {
                // We are missing a usable presentation display while multiple displays exist.
                // Keep interaction on the current display and surface a clear error.
                ReassertResult(
                    Status.ERROR,
                    "Display session is degraded. Missing a valid presentation display. " +
                        "availableDisplays=${state.availableDisplayIds}"
                )
            }
        }
    }

    /**
     * True if our interaction host task exists and is on the given display (API 31+).
     * On older APIs we cannot determine display, so returns false to allow launch.
     */
    @Suppress("DEPRECATION")
    private fun isInteractionOnDisplay(displayId: Int): Boolean {
        if (Build.VERSION.SDK_INT < 31) return false
        val activityManager = activity.getSystemService(ActivityManager::class.java) ?: return false
        val expectedComponent = ComponentName(activity, InteractionHostActivity::class.java)
        val tasks = activityManager.getRunningTasks(10)
        for (task in tasks) {
            if (task.topActivity == expectedComponent || task.baseActivity == expectedComponent) {
                val taskDisplayId = getTaskDisplayId(task)
                return taskDisplayId != null && taskDisplayId == displayId
            }
        }
        return false
    }

    /**
     * True if our presentation host task exists and is on the given display (API 31+).
     * On older APIs we cannot determine display, so returns false to allow launch.
     */
    @Suppress("DEPRECATION")
    private fun isPresentationOnDisplay(displayId: Int): Boolean {
        if (Build.VERSION.SDK_INT < 31) return false
        val activityManager = activity.getSystemService(ActivityManager::class.java) ?: return false
        val expectedComponent = ComponentName(activity, PresentationHostActivity::class.java)
        val tasks = activityManager.getRunningTasks(10)
        for (task in tasks) {
            if (task.topActivity == expectedComponent || task.baseActivity == expectedComponent) {
                val taskDisplayId = getTaskDisplayId(task)
                return taskDisplayId != null && taskDisplayId == displayId
            }
        }
        return false
    }

    @Suppress("DEPRECATION")
    private fun getTaskDisplayId(task: ActivityManager.RunningTaskInfo): Int? {
        return try {
            val field = ActivityManager.RunningTaskInfo::class.java.getField("displayId")
            field.getInt(task)
        } catch (e: Exception) {
            null
        }
    }
}
