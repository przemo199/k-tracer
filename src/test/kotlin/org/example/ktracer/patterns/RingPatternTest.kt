package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import kotlin.test.Test
import kotlin.test.assertEquals

class RingPatternTest {
    @Test
    fun `ring pattern extends in x and y`() {
        val pattern = RingPattern(Color.WHITE, Color.BLACK)
        assertEquals(Color.WHITE, pattern.colorAt(Point.ORIGIN))
        assertEquals(Color.BLACK, pattern.colorAt(Point(1, 0, 0)))
        assertEquals(Color.BLACK, pattern.colorAt(Point(0, 0, 1)))
        assertEquals(Color.BLACK, pattern.colorAt(Point(0.708, 0, 0.708)))
    }
}
