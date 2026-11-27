package org.example.ktracer.colorproviders

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.ktracer.colorproviders.patterns.GradientPattern
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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

    /** Unlike the book's, which jumps back to the first color at every integer x, this gradient runs back and forth. */
    @ParameterizedTest
    @CsvSource("1.25, 0.25", "1.75, 0.75", "2.25, 0.75", "-0.25, 0.75", "-1.25, 0.25", "-2.0, 1.0")
    fun `gradient runs back and forth outside unit interval`(x: Double, gray: Double) {
        val pattern = GradientPattern(Color.WHITE, Color.BLACK)
        assertEquals(Color(gray, gray, gray), pattern.colorAt(Point(x, 0.0, 0.0)))
    }
}
