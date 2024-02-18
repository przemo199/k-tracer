package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import kotlin.test.Test
import kotlin.test.assertEquals

class GradientPatternTest {
    @Test
    fun `gradient interpolates between colors`() {
        val pattern = GradientPattern(Color.WHITE, Color.BLACK)
        assertEquals(Color.WHITE, pattern.colorAt(Point.ORIGIN))
        assertEquals(Color(0.75, 0.75, 0.75), pattern.colorAt(Point(0.25, 0, 0)))
        assertEquals(Color(0.5, 0.5, 0.5), pattern.colorAt(Point(0.5, 0, 0)))
        assertEquals(Color(0.25, 0.25, 0.25), pattern.colorAt(Point(0.75, 0, 0)))
        assertEquals(Color.BLACK, pattern.colorAt(Point(1, 0, 0)))
    }
}
