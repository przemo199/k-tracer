package org.example.ktracer.patterns

import org.example.ktracer.composites.Material
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Sphere
import kotlin.test.Test
import kotlin.test.assertEquals

class StripePatternTest {

    @Test
    fun `creating stripe pattern`() {
        val stripePattern = StripePattern(Color.WHITE, Color.BLACK)
        assertEquals(stripePattern.colorA, Color.WHITE)
        assertEquals(stripePattern.colorB, Color.BLACK)
    }

    @Test
    fun `test pattern alternates in x`() {
        val pattern = StripePattern(Color.WHITE, Color.BLACK)
        assertEquals(pattern.colorAt(Point(0, 0, 0)), Color.WHITE)
        assertEquals(pattern.colorAt(Point(0.9, 0, 0)), Color.WHITE)
        assertEquals(pattern.colorAt(Point(1, 0, 0)), Color.BLACK)
        assertEquals(pattern.colorAt(Point(-0.1, 0, 0)), Color.BLACK)
        assertEquals(pattern.colorAt(Point(-1, 0, 0)), Color.BLACK)
        assertEquals(pattern.colorAt(Point(-1.1, 0, 0)), Color.WHITE)
    }

    @Test
    fun `test pattern is constant in y`() {
        val pattern = StripePattern(Color.WHITE, Color.BLACK)
        assertEquals(pattern.colorAt(Point(0, 0, 0)), Color.WHITE)
        assertEquals(pattern.colorAt(Point(0, 1, 0)), Color.WHITE)
        assertEquals(pattern.colorAt(Point(0, 2, 0)), Color.WHITE)
    }

    @Test
    fun `test pattern is constant in z`() {
        val pattern = StripePattern(Color.WHITE, Color.BLACK)
        assertEquals(pattern.colorAt(Point(0, 0, 0)), Color.WHITE)
        assertEquals(pattern.colorAt(Point(0, 0, 1)), Color.WHITE)
        assertEquals(pattern.colorAt(Point(0, 0, 2)), Color.WHITE)
    }

    @Test
    fun `lighting with stripe pattern applied`() {
        val sphere = Sphere()
        val material = Material().apply {
            pattern = StripePattern(Color.WHITE, Color.BLACK)
            ambient = 1.0
            diffuse = 0.0
            specular = 0.0
        }
        val camera = Vector(0, 0, -1)
        val normal = Vector(0, 0, -1)
        val light = Light(Point(0, 10, -10), Color.WHITE)
        val color1 = material.lighting(sphere, light, Point(0.9, 0, 0), camera, normal, false)
        val color2 = material.lighting(sphere, light, Point(1.1, 0, 0), camera, normal, false)
        assertEquals(Color.WHITE, color1)
        assertEquals(Color.BLACK, color2)
    }

    @Test
    fun `stripe pattern with object transformation`() {
        val sphere = Sphere()
        sphere.transformation = Transformations.scaling(2, 2, 2)
        val pattern = StripePattern(Color.WHITE, Color.BLACK)
        val color = pattern.colorAtShape(sphere, Point(1.5, 0, 0))
        assertEquals(Color.WHITE, color)
    }

    @Test
    fun `stripe pattern with pattern transformation`() {
        val sphere = Sphere()
        val pattern = StripePattern(Color.WHITE, Color.BLACK)
        pattern.transformation = Transformations.scaling(2, 2, 2)
        val color = pattern.colorAtShape(sphere, Point(1.5, 0, 0))
        assertEquals(Color.WHITE, color)
    }

    @Test
    fun `stripe pattern with pattern and object transformation`() {
        val sphere = Sphere()
        sphere.transformation = Transformations.scaling(2, 2, 2)
        val pattern = StripePattern(Color.WHITE, Color.BLACK)
        pattern.transformation = Transformations.translation(0.5, 0, 0)
        val color = pattern.colorAtShape(sphere, Point(2.5, 0, 0))
        assertEquals(Color.WHITE, color)
    }
}
