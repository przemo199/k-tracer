package org.example.ktracer.primitives

import org.example.ktracer.coarseEquals
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VectorTest {
    @Test
    fun `add two vectors`() {
        val vector = Vector(3, -2, 5)
        val vector2 = Vector(-2, 3, 1)
        assertEquals(Vector(1, 1, 6), vector + vector2)
    }

    @Test
    fun `subtract two vectors`() {
        val vector = Vector(3, 2, 1)
        val vector2 = Vector(5, 6, 7)
        assertEquals(Vector(-2, -4, -6), vector - vector2)
    }

    @Test
    fun `subtract vector from zero vector`() {
        val vector = Vector.ZERO
        val vector2 = Vector(1, -2, 3)
        assertEquals(Vector(-1, 2, -3), vector - vector2)
    }

    @Test
    fun `negate vector`() {
        listOf(
            Vector(-0, 0, 0) to Vector(0, 0, 0),
            Vector(1, -2, 3) to Vector(-1, 2, -3),
            Vector(4, -4, 3) to Vector(-4, 4, -3),
        ).forEach { (original, negated) ->
            assertEquals(negated, -original)
        }
    }

    @Test
    fun `multiply vector`() {
        val vector1 = Vector(1, -2, 3)

        assertEquals(Vector(4, -8, 12), vector1 * 4)
        assertEquals(Vector(3.5, -7, 10.5), vector1 * 3.5)
        assertEquals(Vector(0.5, -1, 1.5), vector1 * 0.5)
    }

    @Test
    fun `divide vector`() {
        val vector1 = Vector(1, -2, 3)
        assertEquals(Vector(4, -8, 12), vector1 / 0.25)
        assertEquals(Vector(3.5, -7, 10.5), vector1 / 0.28571428571)
        assertEquals(Vector(0.5, -1, 1.5), vector1 / 2)
    }

    @Test
    fun `vector magnitude`() {
        listOf(
            Vector.RIGHT to 1.0,
            Vector.UP to 1.0,
            Vector.FORWARD to 1.0,
            Vector(1, 2, 3) to sqrt(14.0),
            Vector(-1, -2, -3) to sqrt(14.0),
        ).forEach { (vector, magnitude) ->
            assertEquals(magnitude, vector.magnitude)
        }
    }

    @Test
    fun `normalize vector`() {
        listOf(
            Vector(4, 0, 0) to Vector.RIGHT,
            Vector(0, 4, 0) to Vector.UP,
            Vector(0, 0, 4) to Vector.FORWARD,
            Vector(1, 2, 3) to Vector(0.2672612419124244, 0.5345224838248488, 0.8017837257372732),
        ).forEach { (vector, normal) ->
            assertEquals(normal, vector.normalized())
        }
    }

    @Test
    fun `magnitude of a normalized vector`() {
        val vector = Vector(1, 2, 3)
        assertTrue(1.0 coarseEquals vector.normalized().magnitude)
    }

    @Test
    fun `vector dot product`() {
        val vector1 = Vector(1, 2, 3)
        val vector2 = Vector(2, 3, 4)
        assertEquals(vector1 dot vector2, 20.0)
    }

    @Test
    fun `vector cross product`() {
        val vector1 = Vector(1, 2, 3)
        val vector2 = Vector(2, 3, 4)
        assertEquals(Vector(-1, 2, -1), vector1 cross vector2)
        assertEquals(Vector(1, -2, 1), vector2 cross vector1)
    }

    @Test
    fun `reflected vector`() {
        val vector = Vector(1, -1, 0)
        val normal = Vector(0, 1, 0)
        val reflected = vector.reflect(normal)
        assertEquals(Vector(1, 1, 0), reflected)
    }

    @Test
    fun `reflected vector on a slanted surface`() {
        val vector = Vector(0, -1, 0)
        val normal = Vector(sqrt(2.0) / 2.0, sqrt(2.0) / 2.0, 0)
        val reflected = vector.reflect(normal)
        assertEquals(Vector(1, 0, 0), reflected)
    }
}
