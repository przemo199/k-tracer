package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckerPatternTest {
    @Test
    fun `checker pattern repeats in x`() {
        val pattern = CheckerPattern(Color.WHITE, Color.BLACK)
        assertEquals(Color.WHITE, pattern.colorAt(Point.ORIGIN))
        assertEquals(Color.WHITE, pattern.colorAt(Point(0.99, 0, 0)))
        assertEquals(Color.BLACK, pattern.colorAt(Point(1.01, 0, 0)))
    }

    @Test
    fun `checker pattern repeats in y`() {
        val pattern = CheckerPattern(Color.WHITE, Color.BLACK)
        assertEquals(Color.WHITE, pattern.colorAt(Point.ORIGIN))
        assertEquals(Color.WHITE, pattern.colorAt(Point(0, 0.99, 0)))
        assertEquals(Color.BLACK, pattern.colorAt(Point(0, 1.01, 0)))
    }

    @Test
    fun `checker pattern repeats in z`() {
        val pattern = CheckerPattern(Color.WHITE, Color.BLACK)
        assertEquals(Color.WHITE, pattern.colorAt(Point.ORIGIN))
        assertEquals(Color.WHITE, pattern.colorAt(Point(0, 0, 0.99)))
        assertEquals(Color.BLACK, pattern.colorAt(Point(0, 0, 1.01)))
    }
}
