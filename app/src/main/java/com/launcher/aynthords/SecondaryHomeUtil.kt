package com.launcher.aynthords

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo

object SecondaryHomeUtil {

    data class Candidate(
        val component: ComponentName,
        val priority: Int
    )

    fun querySecondaryHomeCandidates(context: Context): List<Candidate> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.SECONDARY_HOME")

        val results: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)

        return results.mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            val comp = ComponentName(ai.packageName, ai.name)
            Candidate(comp, ri.filter?.priority ?: 0)
        }.sortedByDescending { it.priority }
    }

    /**
     * Disable every SECONDARY_HOME candidate except the package we want to keep.
     * This is the deterministic "just work" method.
     */
    fun buildDisableCommands(keepPackage: String, candidates: List<Candidate>): List<String> {
        return candidates
            .filter { it.component.packageName != keepPackage }
            .map { c ->
                "pm disable-user --user 0 ${c.component.packageName}/${c.component.className}"
            }
    }

    fun buildEnableCommands(keepPackage: String, candidates: List<Candidate>): List<String> {
        return candidates
            .filter { it.component.packageName != keepPackage }
            .map { c ->
                "pm enable ${c.component.packageName}/${c.component.className}"
            }
    }
}
