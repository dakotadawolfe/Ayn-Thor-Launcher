package com.launcher.aynthords.domain.model

import kotlinx.serialization.Serializable

/**
 * Logical view of the current display/session configuration.
 *
 * This is intentionally boring and platform-agnostic so that both the shell layer and
 * feature layers can reason about dual-screen behavior without Android APIs.
 */
data class DisplayState(
    /**
     * Mapping from logical surface role to the physical display id currently assigned.
     * A role may be unmapped (null) in degraded or single-screen scenarios.
     */
    val roleMap: Map<SurfaceRole, Int?> = emptyMap(),
    /**
     * High-level session status derived from the current hardware + mapping.
     */
    val sessionStatus: SessionStatus = SessionStatus.SINGLE_SCREEN,
    /**
     * Snapshot of all known physical display ids at the time this state was computed.
     * Useful for logging and debugging, not for control flow.
     */
    val availableDisplayIds: List<Int> = emptyList(),
)

/**
 * High-level description of how many displays we effectively have and whether we're missing roles.
 */
@Serializable
enum class SessionStatus {
    SINGLE_SCREEN,
    DUAL_SCREEN,
    DEGRADED
}

@Serializable
enum class SurfaceRole {
    INTERACTION,
    PRESENTATION
}
