package org.example.ktracer.shapes

import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TriangleTest {
    @Test
    fun `creating triangle`() {
        val p1 = Point(0, 1, 0)
        val p2 = Point(-1, 0, 0)
        val p3 = Point(1, 0, 0)
        val triangle = Triangle(p1, p2, p3)
        assertEquals(p1, triangle.vertex1)
        assertEquals(p2, triangle.vertex2)
        assertEquals(p3, triangle.vertex3)
        assertEquals(Vector(-1, -1, 0), triangle.edge1)
        assertEquals(Vector(1, -1, 0), triangle.edge2)
        assertEquals(Vector.BACKWARD, triangle.normal)
    }

    @Test
    fun `normal on triangle`() {
        val triangle = Triangle(Point(0, 1, 0), Point(-1, 0, 0), Point(1, 0, 0))
        assertEquals(triangle.normal, triangle.normalAt(Point(0, 0.5, 0)))
        assertEquals(triangle.normal, triangle.normalAt(Point(-0.5, 0.75, 0)))
        assertEquals(triangle.normal, triangle.normalAt(Point(0.5, 0.25, 0)))
    }

    @Test
    fun `ray parallel to triangle`() {
        val triangle = Triangle(Point(0, 1, 0), Point(-1, 0, 0), Point(1, 0, 0))
        val ray = Ray(Point(0, -1, -2), Vector.UP)
        assertNull(triangle.localIntersect(ray))
    }

    @Test
    fun `ray misses p1 p3 edge`() {
        val triangle = Triangle(Point(0, 1, 0), Point(-1, 0, 0), Point(1, 0, 0))
        val ray = Ray(Point(1, 1, -2), Vector.FORWARD)
        assertNull(triangle.localIntersect(ray))
    }

    @Test
    fun `ray misses p1 p2 edge`() {
        val triangle = Triangle(Point(0, 1, 0), Point(-1, 0, 0), Point(1, 0, 0))
        val ray = Ray(Point(-1, 1, -2), Vector.FORWARD)
        assertNull(triangle.localIntersect(ray))
    }

    @Test
    fun `ray misses p2 p3 edge`() {
        val triangle = Triangle(Point(0, 1, 0), Point(-1, 0, 0), Point(1, 0, 0))
        val ray = Ray(Point(0, -1, -2), Vector.FORWARD)
        assertNull(triangle.localIntersect(ray))
    }

    @Test
    fun `ray intersects p2 p3 edge`() {
        val triangle = Triangle(Point(0, 1, 0), Point(-1, 0, 0), Point(1, 0, 0))
        val ray = Ray(Point(0, 0.5, -2), Vector.FORWARD)
        val intersections = triangle.localIntersect(ray)!!
        assertEquals(1, intersections.size)
        assertEquals(2.0, intersections[0].distance)
    }

    @Test
    fun `triangle has bounding box`() {
        val triangle = Triangle(Point(-3, 7, 2), Point(6, 2, -4), Point(2, -1, -1))
        val boundingBox = triangle.boundingBox()
        assertEquals(Point(-3, -1, -4), boundingBox.min)
        assertEquals(Point(6, 7, 2), boundingBox.max)
    }
}
