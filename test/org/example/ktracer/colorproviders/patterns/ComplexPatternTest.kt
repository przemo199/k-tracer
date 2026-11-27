package org.example.ktracer.colorproviders.patterns

import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.shapes.Sphere
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ComplexPatternTest {
    private val a = Color.RED
    private val b = Color.BLUE
    private val redOrBlue = ComplexPattern(StripePattern(a, a), StripePattern(b, b))

    @ParameterizedTest
    @CsvSource("0.0, true", "0.9, true", "1.0, false", "1.9, false", "2.0, true", "-0.1, false", "-1.0, false", "-1.1, true")
    fun `alternates between patterns along x`(x: Double, isA: Boolean) {
        assertEquals(if (isA) a else b, redOrBlue.colorAt(Point(x, 0.0, 0.0)))
    }

    @Test
    fun `is constant along y and z`() {
        assertEquals(a, redOrBlue.colorAt(Point(0.5, 7, -3)))
        assertEquals(b, redOrBlue.colorAt(Point(1.5, -7, 3)))
    }

    @Test
    fun `sub-pattern transformations apply`() {
        val stripes = StripePattern(Color.WHITE, Color.BLACK, Transformations.scaling(0.5, 1, 1))
        val pattern = ComplexPattern(stripes, StripePattern(b, b))
        // x = 0.75 is in the first pattern; its half-width stripes put it in the second stripe
        assertEquals(Color.BLACK, pattern.colorAt(Point(0.75, 0, 0)))
        assertEquals(Color.WHITE, pattern.colorAt(Point(0.25, 0, 0)))
    }

    @Test
    fun `own transformation applies`() {
        val pattern = ComplexPattern(redOrBlue.patternA, redOrBlue.patternB, Transformations.scaling(2, 1, 1))
        assertEquals(a, pattern.colorAtShape(Sphere(), Point(1.5, 0, 0)))
    }
}
