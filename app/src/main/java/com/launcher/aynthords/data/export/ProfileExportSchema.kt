package com.launcher.aynthords.data.export

import com.launcher.aynthords.domain.model.GameProfile
import kotlinx.serialization.Serializable

@Serializable
data class ExportPayload(
    val schemaVersion: Int = 1,
    val profiles: List<GameProfile> = emptyList(),
    val collections: Map<String, List<String>> = emptyMap(),
) {
    fun collectionsAsSets(): Map<String, Set<String>> =
        collections.mapValues { it.value.toSet() }
}
