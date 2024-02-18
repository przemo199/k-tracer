package org.example.ktracer.shapes

import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.example.ktracer.MAX
import org.example.ktracer.MIN

class PlaneTest {
    @Test
    fun `normal is constant`() {
        val plane = Plane()
        val trueNormal = Vector(0, 1, 0)
        val normal1 = plane.normalAt(Point(0, 0, 0))
        val normal2 = plane.normalAt(Point(10, 0, -10))
        val normal3 = plane.normalAt(Point(-5, 0, 150))
        assertEquals(trueNormal, normal1)
        assertEquals(trueNormal, normal2)
        assertEquals(trueNormal, normal3)
    }

    @Test
    fun `ray doesn't intersect plane in parallel`() {
        val plane = Plane()
        val ray = Ray(Point(0, 10, 0), Vector(0, 0, 1))
        val intersections = plane.localIntersect(ray)
        assertNull(intersections)
    }

    @Test
    fun `ray intersects plane from above`() {
        val plane = Plane()
        val ray = Ray(Point(0, 1, 0), Vector(0, -1, 0))
        val intersections = plane.localIntersect(ray)!!
        assertEquals(1, intersections.size)
        assertEquals(1.0, intersections[0].distance)
        assertEquals(plane, intersections[0].shape)
    }

    @Test
    fun `ray intersects plane from below`() {
        val plane = Plane()
        val ray = Ray(Point(0, -1, 0), Vector(0, 1, 0))
        val intersections = plane.localIntersect(ray)!!
        assertEquals(1, intersections.size)
        assertEquals(1.0, intersections[0].distance)
        assertEquals(plane, intersections[0].shape)
    }

    @Test
    fun `plane has bounding box`() {
        val plane = Plane()
        val boundingBox = plane.boundingBox()
        assertEquals(Point(MIN, 0, MIN), boundingBox.min)
        assertEquals(Point(MAX, 0, MAX), boundingBox.max)
    }
}
