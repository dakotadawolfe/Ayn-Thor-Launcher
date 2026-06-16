package com.launcher.aynthords.feature.launcher

/**
 * Default grid column count for the launcher game grid.
 *
 * In v1 this is hardcoded, but the value is centralized here so it can
 * later be driven by LayoutSpec or theme configuration.
 */
const val DEFAULT_GRID_COLUMNS: Int = 3

/**
 * Pure function that computes the next index when moving focus in a grid.
 *
 * Behavior (boring console navigation, no wrap):
 * - Left/Right: clamp within the current row (index ± 1, same row).
 * - Up/Down: move by ±columns (previous/next row).
 * - No wrap-around at edges; returns null when clamped.
 *
 * - [currentIndex]: zero-based index of the currently focused item.
 * - [direction]: DPAD direction.
 * - [columns]: number of columns in the grid (must be >= 1).
 * - [itemCount]: total number of items in the grid.
 *
 * Returns the next index in range [0, itemCount) or null if movement
 * is clamped at an edge (no wrap-around).
 */
fun gridNavigate(
    currentIndex: Int,
    direction: Direction,
    columns: Int,
    itemCount: Int
): Int? {
    if (columns <= 0 || itemCount <= 0) return null
    if (currentIndex !in 0 until itemCount) return null

    return when (direction) {
        Direction.Left -> {
            val col = currentIndex % columns
            if (col == 0) {
                null
            } else {
                currentIndex - 1
            }
        }

        Direction.Right -> {
            val col = currentIndex % columns
            val nextIndex = currentIndex + 1
            if (col == columns - 1 || nextIndex >= itemCount) {
                null
            } else {
                nextIndex
            }
        }

        Direction.Up -> {
            val nextIndex = currentIndex - columns
            if (nextIndex < 0) null else nextIndex
        }

        Direction.Down -> {
            val nextIndex = currentIndex + columns
            if (nextIndex >= itemCount) null else nextIndex
        }
    }
}

