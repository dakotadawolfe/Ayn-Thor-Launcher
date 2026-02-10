package com.launcher.aynthords.display

import android.util.Log
import com.launcher.aynthords.DisplayAppLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "DisplayRoleStore"

enum class SurfaceRole {
    INTERACTION,
    PRESENTATION,
}

enum class PhysicalSurface {
    TOP,
    BOTTOM,
}

enum class ChangeSource {
    STARTUP,
    USER_SWAP,
    RECOVERY,
}

enum class DisplayValidationStatus {
    UNKNOWN,
    VALID,
    INVALID,
}

data class DisplayValidation(
    val top: DisplayValidationStatus,
    val bottom: DisplayValidationStatus,
) {
    val bothValid: Boolean
        get() = top == DisplayValidationStatus.VALID && bottom == DisplayValidationStatus.VALID
}

data class RoleMapping(
    val top: SurfaceRole,
    val bottom: SurfaceRole,
) {
    fun roleFor(surface: PhysicalSurface): SurfaceRole = when (surface) {
        PhysicalSurface.TOP -> top
        PhysicalSurface.BOTTOM -> bottom
    }

    fun swapped(): RoleMapping = RoleMapping(top = bottom, bottom = top)

    companion object {
        val Default = RoleMapping(
            top = SurfaceRole.INTERACTION,
            bottom = SurfaceRole.PRESENTATION,
        )
    }
}

sealed interface DisplayRoleState {
    val currentMapping: RoleMapping
    val sourceOfChange: ChangeSource
    val validation: DisplayValidation

    data class Active(
        override val currentMapping: RoleMapping,
        override val sourceOfChange: ChangeSource,
        override val validation: DisplayValidation,
    ) : DisplayRoleState

    data class Rejected(
        override val currentMapping: RoleMapping,
        override val sourceOfChange: ChangeSource,
        override val validation: DisplayValidation,
        val attemptedMapping: RoleMapping,
        val reason: String,
    ) : DisplayRoleState
}

object DisplayRoleStore {
    private val _state: MutableStateFlow<DisplayRoleState> = MutableStateFlow(
        DisplayRoleState.Active(
            currentMapping = RoleMapping.Default,
            sourceOfChange = ChangeSource.STARTUP,
            validation = DisplayValidation(
                top = DisplayValidationStatus.UNKNOWN,
                bottom = DisplayValidationStatus.UNKNOWN,
            ),
        ),
    )
    val state: StateFlow<DisplayRoleState> = _state.asStateFlow()

    fun swapScreens(source: ChangeSource, displayId: Int?) {
        val physicalSurface = physicalSurfaceForDisplayId(displayId)
        if (physicalSurface == null) {
            rejectCurrent("swap requested from unsupported displayId=$displayId", source)
            return
        }

        val nextMapping = _state.value.currentMapping.swapped()
        transitionTo(nextMapping, source)
    }

    fun reportDisplayValidation(displayId: Int?, source: ChangeSource = ChangeSource.RECOVERY) {
        val surface = physicalSurfaceForDisplayId(displayId)
        val current = _state.value
        val nextValidation = when (surface) {
            PhysicalSurface.TOP -> current.validation.copy(top = DisplayValidationStatus.VALID)
            PhysicalSurface.BOTTOM -> current.validation.copy(bottom = DisplayValidationStatus.VALID)
            null -> {
                rejectCurrent("validation from unsupported displayId=$displayId", source)
                return
            }
        }

        _state.value = DisplayRoleState.Active(
            currentMapping = current.currentMapping,
            sourceOfChange = source,
            validation = nextValidation,
        )
    }

    fun markDisplayInvalid(displayId: Int?, source: ChangeSource = ChangeSource.RECOVERY) {
        val surface = physicalSurfaceForDisplayId(displayId)
        val current = _state.value
        val nextValidation = when (surface) {
            PhysicalSurface.TOP -> current.validation.copy(top = DisplayValidationStatus.INVALID)
            PhysicalSurface.BOTTOM -> current.validation.copy(bottom = DisplayValidationStatus.INVALID)
            null -> {
                rejectCurrent("invalid-mark from unsupported displayId=$displayId", source)
                return
            }
        }

        _state.value = DisplayRoleState.Active(
            currentMapping = current.currentMapping,
            sourceOfChange = source,
            validation = nextValidation,
        )
    }

    fun roleForDisplayId(displayId: Int?): SurfaceRole? {
        val surface = physicalSurfaceForDisplayId(displayId) ?: return null
        return _state.value.currentMapping.roleFor(surface)
    }

    private fun transitionTo(nextMapping: RoleMapping, source: ChangeSource) {
        val current = _state.value

        if (!isMappingValid(nextMapping)) {
            rejectCurrent("invalid mapping attempted: top=${nextMapping.top}, bottom=${nextMapping.bottom}", source, nextMapping)
            return
        }

        if (current.validation.top == DisplayValidationStatus.INVALID ||
            current.validation.bottom == DisplayValidationStatus.INVALID
        ) {
            rejectCurrent("swap blocked due to INVALID display validation status", source, nextMapping)
            return
        }

        _state.value = DisplayRoleState.Active(
            currentMapping = nextMapping,
            sourceOfChange = source,
            validation = current.validation,
        )
        Log.i(TAG, "transition accepted source=$source top=${nextMapping.top} bottom=${nextMapping.bottom}")
    }

    private fun isMappingValid(mapping: RoleMapping): Boolean {
        if (mapping.top == mapping.bottom) {
            return false
        }
        val hasInteraction = mapping.top == SurfaceRole.INTERACTION || mapping.bottom == SurfaceRole.INTERACTION
        val hasPresentation = mapping.top == SurfaceRole.PRESENTATION || mapping.bottom == SurfaceRole.PRESENTATION
        return hasInteraction && hasPresentation
    }

    private fun rejectCurrent(reason: String, source: ChangeSource, attempted: RoleMapping? = null) {
        val current = _state.value
        Log.e(TAG, "transition rejected source=$source reason=$reason")
        _state.value = DisplayRoleState.Rejected(
            currentMapping = current.currentMapping,
            sourceOfChange = source,
            validation = current.validation,
            attemptedMapping = attempted ?: current.currentMapping,
            reason = reason,
        )
    }

    private fun physicalSurfaceForDisplayId(displayId: Int?): PhysicalSurface? = when (displayId) {
        DisplayAppLauncher.DISPLAY_TOP -> PhysicalSurface.TOP
        DisplayAppLauncher.DISPLAY_BOTTOM -> PhysicalSurface.BOTTOM
        else -> null
    }
}
