package org.example.ktracer.colorproviders.patterns

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.shapes.Group
import org.example.ktracer.shapes.Sphere

class PatternTest {
    @Test
    fun `default pattern transformation`() {
        assertEquals(Transformation.identity(), TestPattern().transformation)
    }

    @Test
    fun `pattern with object transformation`() {
        val sphere = Sphere(transformation = Transformations.scaling(2, 2, 2))
        assertEquals(Color(1, 1.5, 2), TestPattern().colorAtShape(sphere, Point(2, 3, 4)))
    }

    @Test
    fun `pattern with pattern transformation`() {
        val pattern = TestPattern(Transformations.scaling(2, 2, 2))
        assertEquals(Color(1, 1.5, 2), pattern.colorAtShape(Sphere(), Point(2, 3, 4)))
    }

    @Test
    fun `pattern with pattern and object transformations`() {
        val sphere = Sphere(transformation = Transformations.scaling(2, 2, 2))
        val pattern = TestPattern(Transformations.translation(0.5, 1, 1.5))
        assertEquals(Color(0.75, 0.5, 0.25), pattern.colorAtShape(sphere, Point(2.5, 3, 3.5)))
    }

    @Test
    fun `pattern on a shape in a group goes through the group's transformation`() {
        val sphere = Sphere(transformation = Transformations.translation(5, 0, 0))
        Group(transformation = Transformations.scaling(2, 2, 2)).add(sphere)
        // world (10, 4, 6) -> group space (5, 2, 3) -> sphere's object space (0, 2, 3)
        assertEquals(Color(0, 2, 3), TestPattern().colorAtShape(sphere, Point(10, 4, 6)))
    }

    @Test
    fun `patterns compare by value`() {
        val stripes = StripePattern(Color.WHITE, Color.BLACK, Transformations.scaling(2, 2, 2))
        assertEquals(StripePattern(Color.WHITE, Color.BLACK, Transformations.scaling(2, 2, 2)), stripes)
        assertNotEquals<Pattern>(StripePattern(Color.WHITE, Color.BLACK), stripes)
        assertNotEquals<Pattern>(RingPattern(Color.WHITE, Color.BLACK), StripePattern(Color.WHITE, Color.BLACK))
    }
}
