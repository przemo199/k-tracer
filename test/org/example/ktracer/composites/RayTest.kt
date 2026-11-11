package org.example.ktracer.composites

import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Sphere
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RayTest {
    @Test
    fun `creating and inspecting ray`() {
        val origin = Point(1, 2, 3)
        val direction = Vector(4, 5, 6)
        val ray = Ray(origin, direction)
        assertEquals(origin, ray.origin)
        assertEquals(direction, ray.direction)
    }

    @Test
    fun `computing a point from a distance`() {
        val ray = Ray(Point(2, 3, 4), Vector.RIGHT)
        assertEquals(Point(2, 3, 4), ray.position(0))
        assertEquals(Point(3, 3, 4), ray.position(1))
        assertEquals(Point(1, 3, 4), ray.position(-1))
        assertEquals(Point(4.5, 3, 4), ray.position(2.5))
    }

    @Test
    fun `intersections in the middle of sphere`() {
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val sphere = Sphere()
        val intersections = Intersections()
        ray.intersect(sphere, intersections)
        assertEquals(2, intersections.size)
        assertEquals(4.0, intersections[0].distance)
        assertEquals(6.0, intersections[1].distance)
        assertEquals(sphere, intersections[0].shape)
        assertEquals(sphere, intersections[1].shape)
    }

    @Test
    fun `intersections on the edge of sphere`() {
        val ray = Ray(Point(0, 1, -5), Vector.FORWARD)
        val sphere = Sphere()
        val intersections = Intersections()
        ray.intersect(sphere, intersections)
        assertEquals(2, intersections.size)
        assertEquals(5.0, intersections[0].distance)
        assertEquals(5.0, intersections[1].distance)
        assertEquals(sphere, intersections[0].shape)
        assertEquals(sphere, intersections[1].shape)
    }

    @Test
    fun `no intersections`() {
        val ray = Ray(Point(0, 2, -5), Vector.FORWARD)
        val sphere = Sphere()
        val intersections = Intersections()
        ray.intersect(sphere, intersections)
        assertTrue(intersections.isEmpty())
    }

    @Test
    fun `ray origin inside sphere`() {
        val ray = Ray(Point.ORIGIN, Vector.FORWARD)
        val sphere = Sphere()
        val intersections = Intersections()
        ray.intersect(sphere, intersections)
        assertEquals(2, intersections.size)
        assertEquals(-1.0, intersections[0].distance)
        assertEquals(1.0, intersections[1].distance)
        assertEquals(sphere, intersections[0].shape)
        assertEquals(sphere, intersections[1].shape)
    }

    @Test
    fun `ray origin behind sphere`() {
        val ray = Ray(Point(0, 0, 5), Vector.FORWARD)
        val sphere = Sphere()
        val intersections = Intersections()
        ray.intersect(sphere, intersections)
        assertEquals(2, intersections.size)
        assertEquals(-6.0, intersections[0].distance)
        assertEquals(-4.0, intersections[1].distance)
        assertEquals(sphere, intersections[0].shape)
        assertEquals(sphere, intersections[1].shape)
    }

    @Test
    fun `ray translation`() {
        val ray = Ray(Point(1, 2, 3), Vector.UP)
        val matrix = Transformations.translation(3, 4, 5)
        val transformedRay = ray.transform(matrix)
        assertEquals(Point(4, 6, 8), transformedRay.origin)
        assertEquals(Vector.UP, transformedRay.direction)
    }

    @Test
    fun `ray scaling`() {
        val ray = Ray(Point(1, 2, 3), Vector.UP)
        val matrix = Transformations.scaling(2, 3, 4)
        val transformedRay = ray.transform(matrix)
        assertEquals(Point(2, 6, 12), transformedRay.origin)
        assertEquals(Vector(0, 3, 0), transformedRay.direction)
    }

    @Test
    fun `intersecting scaled sphere`() {
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val sphere = Sphere().apply {
            transformation = Transformations.scaling(2, 2, 2)
        }
        val intersections = Intersections()
        ray.intersect(sphere, intersections)
        assertEquals(2, intersections.size)
        assertEquals(3.0, intersections[0].distance)
        assertEquals(7.0, intersections[1].distance)
    }

    @Test
    fun `intersecting translated sphere`() {
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val sphere = Sphere()
        sphere.transformation = Transformations.translation(5, 0, 0)
        val intersections = Intersections()
        ray.intersect(sphere, intersections)
        assertTrue(intersections.isEmpty())
    }
}
