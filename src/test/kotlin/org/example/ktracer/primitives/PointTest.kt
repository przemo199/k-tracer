package org.example.ktracer.primitives

import org.example.ktracer.EPSILON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PointTest {
    @Test
    fun `point equality`() {
        val point1 = Point(4, -4, 3)
        val point2 = point1
        val point3 = Point(4.1 + EPSILON, -4, 3)
        val point4 = Point(4.0 + EPSILON - (EPSILON / 2.0), -4, 3)
        assertEquals(point1, point2)
        assertNotEquals(point2, point3)
        assertEquals(point2, point4)
    }

    @Test
    fun `add point and vector`() {
        val point = Point(3, -2, 5)
        val vector = Vector(-2, 3, 1)
        assertEquals(Point(1, 1, 6), point + vector)
    }

    @Test
    fun `subtract two points`() {
        val point = Point(3, 2, 1)
        val point2 = Point(5, 6, 7)
        assertEquals(Vector(-2, -4, -6), point - point2)
    }

    @Test
    fun `subtract vector from point`() {
        val point = Point(3, 2, 1)
        val vector = Vector(5, 6, 7)
        assertEquals(Point(-2, -4, -6), point - vector)
    }

    @Test
    fun `negate point`() {
        listOf(
            Point(-0, 0, 0) to Point(0, 0, 0),
            Point(1, -2, 3) to Point(-1, 2, -3),
            Point(4, -4, 3) to Point(-4, 4, -3),
        ).forEach { (original, negated) ->
            assertEquals(negated, -original)
        }
    }

    @Test
    fun `multiply point`() {
        val point = Point(1, -2, 3)
        assertEquals(Point(4, -8, 12), point * 4)
        assertEquals(Point(3.5, -7, 10.5), point * 3.5)
        assertEquals(Point(0.5, -1, 1.5), point * 0.5)
    }

    @Test
    fun `divide point`() {
        val point = Point(1, -2, 3)
        assertEquals(Point(4, -8, 12), point / 0.25)
        assertEquals(Point(3.5, -7, 10.5), point / 0.28571428571)
        assertEquals(Point(0.5, -1, 1.5), point / 2)
    }
}
