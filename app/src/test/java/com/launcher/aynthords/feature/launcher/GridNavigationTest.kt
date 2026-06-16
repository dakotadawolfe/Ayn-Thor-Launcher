package com.launcher.aynthords.feature.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GridNavigationTest {

    @Test
    fun `left and right move within row and clamp at edges`() {
        val columns = 3
        val itemCount = 9 // 3x3 grid

        // Middle of row
        assertEquals(0, gridNavigate(currentIndex = 1, direction = Direction.Left, columns = columns, itemCount = itemCount))
        assertEquals(2, gridNavigate(currentIndex = 1, direction = Direction.Right, columns = columns, itemCount = itemCount))

        // Clamp at left edge
        assertNull(gridNavigate(currentIndex = 0, direction = Direction.Left, columns = columns, itemCount = itemCount))

        // Clamp at right edge
        assertNull(gridNavigate(currentIndex = 2, direction = Direction.Right, columns = columns, itemCount = itemCount))
    }

    @Test
    fun `right movement respects last partial row`() {
        val columns = 3
        val itemCount = 5 // indices: 0,1,2,3,4 (last row has 2 items)

        // From index 3 we can move right to 4
        assertEquals(4, gridNavigate(currentIndex = 3, direction = Direction.Right, columns = columns, itemCount = itemCount))

        // From index 4 we cannot move right (would go out of bounds)
        assertNull(gridNavigate(currentIndex = 4, direction = Direction.Right, columns = columns, itemCount = itemCount))
    }

    @Test
    fun `up and down move by rows and clamp at top and bottom`() {
        val columns = 3
        val itemCount = 10 // indices: 0-9

        // Down one row
        assertEquals(3, gridNavigate(currentIndex = 0, direction = Direction.Down, columns = columns, itemCount = itemCount))
        assertEquals(4, gridNavigate(currentIndex = 1, direction = Direction.Down, columns = columns, itemCount = itemCount))

        // Up one row
        assertEquals(1, gridNavigate(currentIndex = 4, direction = Direction.Up, columns = columns, itemCount = itemCount))

        // Clamp at top
        assertNull(gridNavigate(currentIndex = 0, direction = Direction.Up, columns = columns, itemCount = itemCount))

        // Clamp at bottom: from last full row to last row
        assertEquals(9, gridNavigate(currentIndex = 6, direction = Direction.Down, columns = columns, itemCount = itemCount))
        // From last item, cannot move further down
        assertNull(gridNavigate(currentIndex = 9, direction = Direction.Down, columns = columns, itemCount = itemCount))
    }

    @Test
    fun `single item grid does not move`() {
        val columns = 3
        val itemCount = 1

        assertNull(gridNavigate(currentIndex = 0, direction = Direction.Left, columns = columns, itemCount = itemCount))
        assertNull(gridNavigate(currentIndex = 0, direction = Direction.Right, columns = columns, itemCount = itemCount))
        assertNull(gridNavigate(currentIndex = 0, direction = Direction.Up, columns = columns, itemCount = itemCount))
        assertNull(gridNavigate(currentIndex = 0, direction = Direction.Down, columns = columns, itemCount = itemCount))
    }

    @Test
    fun `invalid indices or empty grid result in no movement`() {
        val columns = 3

        // Empty grid
        assertNull(gridNavigate(currentIndex = 0, direction = Direction.Right, columns = columns, itemCount = 0))

        // Out of range index
        assertNull(gridNavigate(currentIndex = -1, direction = Direction.Right, columns = columns, itemCount = 5))
        assertNull(gridNavigate(currentIndex = 5, direction = Direction.Right, columns = columns, itemCount = 5))
    }
}

