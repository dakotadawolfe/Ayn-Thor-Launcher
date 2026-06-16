package com.launcher.aynthords.theme.spec

import com.launcher.aynthords.domain.model.SurfaceRole
import kotlinx.serialization.Serializable

@Serializable
data class LayoutSpecV1(
    val schemaVersion: Int = 1,
    val layouts: Map<String, SurfaceLayoutSpec> = emptyMap()
) {
    /** Layout for a surface role; keys in JSON are "INTERACTION" and "PRESENTATION". */
    fun layoutFor(role: SurfaceRole): SurfaceLayoutSpec? = layouts[role.name]
}

@Serializable
data class SurfaceLayoutSpec(
    val root: LayoutNode,
    /** Non-logic overlays drawn in order (e.g. backgrounds, frames). Rendered in a Box before the root. */
    val extras: List<ExtraElement> = emptyList()
)

/** Theme-defined overlay: image, text, or shape. pos/size in normalized 0..1 (ES-style). */
@Serializable
data class ExtraElement(
    val type: ExtraElementType,
    val source: String = "",
    val posX: Float = 0f,
    val posY: Float = 0f,
    val sizeX: Float = 1f,
    val sizeY: Float = 1f,
    val order: Int = 0,
    val params: Map<String, String> = emptyMap()
)

@Serializable
enum class ExtraElementType {
    Image,
    Text,
    Shape
}

@Serializable
data class LayoutNode(
    val type: NodeType,
    val children: List<LayoutNode> = emptyList(),
    val primitive: Primitive? = null, // Only for leaf nodes
    val weight: Float = 0f, // For weighted layouts like Row/Column; 0 = no weight (size by content)
    val params: Map<String, String> = emptyMap() // Optional parameters
)

@Serializable
enum class NodeType {
    Row, Column, Stack, RegionSplit // RegionSplit is a placeholder for a more complex layout
}

@Serializable
enum class Primitive {
    GameGrid,
    Rail,
    HintBar,
    BackgroundArt,
    HeroArt,
    MetadataPanel,
    SelectionIndicator,
    DetailsPanel,
}
