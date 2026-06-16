package com.launcher.aynthords.shell.display

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.SystemClock
import com.launcher.aynthords.DebugLog
import com.launcher.aynthords.R
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.InteractionHostActivity
import com.launcher.aynthords.PresentationHostActivity

/**
 * Playful swap animation (scale + fade with overshoot) for home-console feel.
 */
private fun Activity.makeSwapActivityOptions(displayId: Int): ActivityOptions {
    return ActivityOptions.makeCustomAnimation(this, R.anim.swap_enter, R.anim.swap_exit)
        .apply { setLaunchDisplayId(displayId) }
}

private var lastInteractionEnsureTime = 0L
private var lastPresentationEnsureTime = 0L
private var lastOnPauseTime = 0L
private const val INTERACTION_ENSURE_DEBOUNCE_MS = 2500L

/**
 * Launch or bring-to-front the interaction host on the given display.
 * Debounced to avoid spurious re-launches when recents is closed (getRunningTasks
 * can return stale data and trigger interactionNeedsLaunch incorrectly).
 */
fun Activity.ensureInteractionOnDisplay(displayId: Int) {
    val now = SystemClock.elapsedRealtime()
    if (now - lastInteractionEnsureTime < INTERACTION_ENSURE_DEBOUNCE_MS) {
        return
    }
    lastInteractionEnsureTime = now

    // #region agent log
    DebugLog.log(applicationContext, "H3", "DisplayLaunch.ensureInteractionOnDisplay", "about to startActivity",
        "displayId" to displayId)
    // #endregion
    val intent = Intent(this, InteractionHostActivity::class.java).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
    }
    startActivity(intent, makeSwapActivityOptions(displayId).toBundle())
    overridePendingTransition(R.anim.swap_enter, R.anim.swap_exit)
}

private const val PRESENTATION_ENSURE_DEBOUNCE_MS = 2500L
private const val RECENTS_RETURN_WINDOW_MS = 4000L

/**
 * Launch or bring-to-front the presentation host on the given display.
 * Debounced to avoid spurious re-launches when recents is closed (getRunningTasks
 * can return stale data and trigger presentationNeedsLaunch incorrectly).
 */
fun Activity.ensureSecondaryDisplayActivity(displayId: Int) {
    val now = SystemClock.elapsedRealtime()
    if (now - lastPresentationEnsureTime < PRESENTATION_ENSURE_DEBOUNCE_MS) {
        return
    }
    lastPresentationEnsureTime = now

    // #region agent log
    DebugLog.log(applicationContext, "H3", "DisplayLaunch.ensureSecondaryDisplayActivity", "about to startActivity",
        "displayId" to displayId)
    // #endregion
    val intent = Intent(this, PresentationHostActivity::class.java).apply {
        addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }
    startActivity(intent, makeSwapActivityOptions(displayId).toBundle())
    overridePendingTransition(R.anim.swap_enter, R.anim.swap_exit)
}

/**
 * Refresh the ensure debounce timestamps. Call when reassert returns HEALTHY or when
 * either host resumes, so that a subsequent spurious reassert (e.g. after recents + HOME)
 * won't trigger ensure* and cause a refresh.
 */
fun refreshEnsureDebounce() {
    val now = SystemClock.elapsedRealtime()
    lastInteractionEnsureTime = now
    lastPresentationEnsureTime = now
}

/**
 * Record that an activity entered onPause. Used to detect quick return from recents.
 */
fun recordOnPause() {
    lastOnPauseTime = SystemClock.elapsedRealtime()
}

/**
 * True if we likely just returned from recents (short pause→resume). When true, we should
 * refresh debounce before reassert to avoid spurious ensure* calls.
 */
fun likelyReturnedFromRecents(): Boolean {
    if (lastOnPauseTime == 0L) return false
    val elapsed = SystemClock.elapsedRealtime() - lastOnPauseTime
    return elapsed in 100..RECENTS_RETURN_WINDOW_MS
}

/**
 * Resolve presentation display from [DisplayRoleMappingStore] and launch there.
 * No-op if no presentation display is mapped.
 */
fun Activity.ensureSecondaryDisplayActivity() {
    val presentationDisplayId = DisplayRoleMappingStore(this)
        .resolveDisplayId(SurfaceRole.PRESENTATION)
        ?: return
    ensureSecondaryDisplayActivity(presentationDisplayId)
}
