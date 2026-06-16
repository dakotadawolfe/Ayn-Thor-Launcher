package com.launcher.aynthords.shell.display

import android.content.Context
import com.launcher.aynthords.domain.model.DisplayState
import com.launcher.aynthords.domain.model.SessionStatus
import com.launcher.aynthords.domain.model.SurfaceRole

/**
 * Canonical, platform-light view of the current display/session configuration.
 *
 * This class owns the logic for:
 * - Building a [DisplayState] from hardware + stored role mapping.
 * - Classifying the session as SINGLE_SCREEN / DUAL_SCREEN / DEGRADED.
 * - Emitting high-level [ReassertAction]s that callers (activities/controllers)
 *   can translate into concrete Android operations.
 *
 * It is intentionally boring and deterministic so it can be reasoned about and
 * unit tested without deep Android knowledge.
 */
class DisplaySessionController(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val registry = DisplayRegistry(appContext)
    private val mappingStore = DisplayRoleMappingStore(appContext)

    /**
     * Last computed state. Call [reassert] to recompute; this is not reactive yet.
     */
    var currentState: DisplayState = DisplayState()
        private set

    /**
     * Result of a reassertion pass.
     *
     * @property status High-level session status after reassertion.
     * @property actions Logical actions the shell should perform to honor the state.
     * @property state The full [DisplayState] snapshot used to compute [status] and [actions].
     */
    data class ReassertResult(
        val status: SessionStatus,
        val actions: List<ReassertAction>,
        val state: DisplayState,
    )

    /**
     * High-level actions that callers can translate into concrete platform calls.
     */
    sealed interface ReassertAction {
        /**
         * Ensure the presentation host is running on [displayId].
         *
         * The action is idempotent: if the host is already present on [displayId],
         * callers should treat this as a no-op.
         */
        data class LaunchPresentationOnDisplay(val displayId: Int) : ReassertAction

        /**
         * Ensure the interaction host is running on [displayId].
         *
         * The action is idempotent: if the host is already present on [displayId],
         * callers should treat this as a no-op.
         */
        data class LaunchInteractionOnDisplay(val displayId: Int) : ReassertAction
    }

    /**
     * Recompute the current [DisplayState] from hardware and stored mapping and
     * derive a deterministic [ReassertResult].
     *
     * [currentHostDisplayId] is accepted for future use (e.g., richer diagnostics),
     * but the controller does not perform side effects based on it. Shell-level
     * controllers such as [DualScreenSessionController] are responsible for
     * interpreting state + actions in the context of the current host.
     */
    fun reassert(currentHostDisplayId: Int): ReassertResult {
        val state = computeDisplayState()
        currentState = state

        val actions = buildList<ReassertAction> {
            if (state.sessionStatus == SessionStatus.DUAL_SCREEN) {
                val interactionDisplayId = state.roleMap[SurfaceRole.INTERACTION]
                val presentationDisplayId = state.roleMap[SurfaceRole.PRESENTATION]
                if (interactionDisplayId != null) {
                    add(ReassertAction.LaunchInteractionOnDisplay(interactionDisplayId))
                }
                if (presentationDisplayId != null) {
                    add(ReassertAction.LaunchPresentationOnDisplay(presentationDisplayId))
                }
            }
        }

        return ReassertResult(
            status = state.sessionStatus,
            actions = actions,
            state = state,
        )
    }

    /**
     * Best-effort helper for callers that only care about the interaction display.
     */
    fun interactionDisplayId(): Int? = currentState.roleMap[SurfaceRole.INTERACTION]

    /**
     * Best-effort helper for callers that only care about the presentation display.
     */
    fun presentationDisplayId(): Int? = currentState.roleMap[SurfaceRole.PRESENTATION]

    /**
     * Compute a fresh [DisplayState] from the current hardware snapshot and stored role mapping.
     *
     * Rules:
     * - If there is only one physical display, we are in [SessionStatus.SINGLE_SCREEN];
     *   interaction is mapped to the best candidate, presentation is unmapped.
     * - If there are 2+ displays and we have distinct, valid interaction + presentation
     *   display ids, we are in [SessionStatus.DUAL_SCREEN].
     * - If we have 2+ displays but cannot resolve a distinct presentation display,
     *   we are in [SessionStatus.DEGRADED] and presentation is left unmapped.
     */
    private fun computeDisplayState(): DisplayState {
        val snapshot = registry.snapshot()
        val allIds = snapshot.allDisplayIds

        if (allIds.size <= 1) {
            // Single-screen: map interaction to the best candidate, leave presentation unmapped.
            val interaction = snapshot.interactionCandidates.firstOrNull()
            return DisplayState(
                roleMap = mapOf(
                    SurfaceRole.INTERACTION to interaction,
                    SurfaceRole.PRESENTATION to null,
                ),
                sessionStatus = SessionStatus.SINGLE_SCREEN,
                availableDisplayIds = allIds,
            )
        }

        // Multi-display: resolve ids via the mapping store so decisions are stable
        // across reboots and hardware changes.
        val interactionDisplayId = mappingStore.resolveDisplayId(SurfaceRole.INTERACTION)
            ?: snapshot.interactionCandidates.firstOrNull()
        val rawPresentationDisplayId = mappingStore.resolveDisplayId(SurfaceRole.PRESENTATION)

        // Never allow presentation to alias the interaction display in the logical state.
        val presentationDisplayId =
            if (rawPresentationDisplayId != null && rawPresentationDisplayId != interactionDisplayId) {
                rawPresentationDisplayId
            } else {
                null
            }

        val roleMap = mapOf(
            SurfaceRole.INTERACTION to interactionDisplayId,
            SurfaceRole.PRESENTATION to presentationDisplayId,
        )

        val sessionStatus = when {
            interactionDisplayId != null && presentationDisplayId != null -> SessionStatus.DUAL_SCREEN
            else -> SessionStatus.DEGRADED
        }

        return DisplayState(
            roleMap = roleMap,
            sessionStatus = sessionStatus,
            availableDisplayIds = allIds,
        )
    }
}

