package com.launcher.aynthords.shell.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import com.launcher.aynthords.domain.model.SurfaceRole

data class DisplaySnapshot(
    val allDisplayIds: List<Int>,
    val interactionCandidates: List<Int>,
    val presentationCandidates: List<Int>
)

class DisplayRegistry(context: Context) {
    private val appContext = context.applicationContext

    fun snapshot(): DisplaySnapshot {
        val dm = appContext.getSystemService(DisplayManager::class.java)
            ?: return DisplaySnapshot(emptyList(), emptyList(), emptyList())

        val displays = dm.displays.toList()
        if (displays.isEmpty()) return DisplaySnapshot(emptyList(), emptyList(), emptyList())

        val defaultDisplayId = displays.firstOrNull { it.displayId == Display.DEFAULT_DISPLAY }?.displayId
        val nonDefaultIds = displays.map { it.displayId }.filterNot { it == Display.DEFAULT_DISPLAY }

        val interactionCandidates = buildList {
            if (defaultDisplayId != null) add(defaultDisplayId)
            addAll(nonDefaultIds)
        }.distinct()

        val presentationCandidates = buildList {
            addAll(nonDefaultIds)
            if (defaultDisplayId != null) add(defaultDisplayId)
        }.distinct()

        return DisplaySnapshot(
            allDisplayIds = displays.map { it.displayId },
            interactionCandidates = interactionCandidates,
            presentationCandidates = presentationCandidates
        )
    }

    fun resolveDefault(role: SurfaceRole): Int? {
        val snapshot = snapshot()
        return when (role) {
            SurfaceRole.INTERACTION -> snapshot.interactionCandidates.firstOrNull()
            SurfaceRole.PRESENTATION -> snapshot.presentationCandidates.firstOrNull()
        }
    }
}
