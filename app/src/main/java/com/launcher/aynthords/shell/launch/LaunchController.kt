package com.launcher.aynthords.shell.launch

import android.app.Activity
import android.content.Intent
import com.launcher.aynthords.domain.model.ResolvedGameModel
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.domain.repo.LibraryRepository
import com.launcher.aynthords.shell.display.DisplayAppLauncher

sealed class LaunchResult {
    data object Success : LaunchResult()
    data class Failure(val reason: LaunchFailure) : LaunchResult()
}

enum class LaunchFailure {
    MissingIntent,
    ActivityNotFound,
    ResolveFailed,
}

class LaunchController(
    private val activity: Activity,
    private val libraryRepository: LibraryRepository,
) {

    fun launch(entry: ResolvedGameModel): LaunchResult {
        val primaryIntent = activity.packageManager.getLaunchIntentForPackage(entry.packageName)
        val intent = primaryIntent ?: Intent(Intent.ACTION_MAIN).apply {
            setPackage(entry.packageName)
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        if (activity.packageManager.resolveActivity(intent, 0) == null) {
            val failure = if (primaryIntent == null) LaunchFailure.MissingIntent else LaunchFailure.ActivityNotFound
            return LaunchResult.Failure(failure)
        }

        val role = entry.launchPolicyOverride?.preferredLogicalRole ?: SurfaceRole.INTERACTION

        return try {
            DisplayAppLauncher.launchOnRole(activity, intent, role)
            libraryRepository.recordLastLaunched(entry.packageName)
            LaunchResult.Success
        } catch (e: Exception) {
            LaunchResult.Failure(LaunchFailure.ResolveFailed)
        }
    }
}
