package org.example.ktracer.composites

import org.example.ktracer.EPSILON
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Plane
import org.example.ktracer.shapes.Sphere
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntersectionTest {
    @Test
    fun `creating and inspecting intersection`() {
        val shape = Sphere()
        val intersection = Intersection(3.5, shape)
        assertEquals(3.5, intersection.distance)
        assertEquals(shape, intersection.shape)
    }

    @Test
    fun `precomputing intersection state`() {
        val ray = Ray(Point(0, 0, -5), Vector(0, 0, 1))
        val shape = Sphere()
        val intersection = Intersection(4.0, shape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        assertEquals(intersection.distance, computedHit.distance)
        assertEquals(shape, computedHit.shape)
        assertEquals(Point(0, 0, -1), computedHit.point)
        assertEquals(Vector(0, 0, -1), computedHit.cameraVector)
        assertEquals(Vector(0, 0, -1), computedHit.normal)
    }

    @Test
    fun `hit with intersection outside`() {
        val ray = Ray(Point(0, 0, -5), Vector(0, 0, 1))
        val shape = Sphere()
        val intersection = Intersection(4.0, shape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        assertFalse(computedHit.isInside)
    }

    @Test
    fun `hit with intersection inside`() {
        val ray = Ray(Point(0, 0, 0), Vector(0, 0, 1))
        val shape = Sphere()
        val intersection = Intersection(1.0, shape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        assertTrue(computedHit.isInside)
        assertEquals(Point(0, 0, 1), computedHit.point)
        assertEquals(Vector(0, 0, -1), computedHit.cameraVector)
    }

    @Test
    fun `hit offsets point`() {
        val ray = Ray(Point(0, 0, -5), Vector(0, 0, 1))
        val shape = Sphere()
        shape.transformation = Transformations.translation(0, 0, 1)
        val intersection = Intersection(5.0, shape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        assertTrue(computedHit.overPoint.z < -EPSILON / 2.0)
        assertTrue(computedHit.overPoint.z < computedHit.point.z)
    }

    @Test
    fun `precomputing reflection vector`() {
        val shape = Plane()
        val ray = Ray(Point(0, 1, -1), Vector(0, -sqrt(2.0) / 2.0, sqrt(2.0) / 2.0))
        val intersection = Intersection(sqrt(2.0), shape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        assertEquals(Vector(0, sqrt(2.0) / 2.0, sqrt(2.0) / 2.0), computedHit.reflectionVector)
    }

    @Test
    fun `finding refractive index`() {
        val shape1 = Sphere().apply {
            material = Material.GLASS
            transformation = Transformations.scaling(2, 2, 2)
            material.refractiveIndex = 1.5
        }

        val shape2 = Sphere().apply {
            material = Material.GLASS
            transformation = Transformations.translation(0, 0, -0.25)
            material.refractiveIndex = 2.0
        }

        val shape3 = Sphere().apply {
            material = Material.GLASS
            transformation = Transformations.translation(0, 0, 0.25)
            material.refractiveIndex = 2.5
        }

        val intersections = Intersections(
            Intersection(2, shape1),
            Intersection(2.75, shape2),
            Intersection(3.25, shape3),
            Intersection(4.75, shape2),
            Intersection(5.25, shape3),
            Intersection(6, shape1),
        )
        val ray = Ray(Point(0, 0, -4), Vector.FORWARD)
        val refractiveIndexes1 = listOf(1.0, 1.5, 2.0, 2.5, 2.5, 1.5)
        val refractiveIndexes2 = listOf(1.5, 2.0, 2.5, 2.5, 1.5, 1.0)
        for (i in 0..<intersections.size) {
            val computedHit = intersections[i].prepareComputations(ray, intersections)
            assertEquals(refractiveIndexes1[i], computedHit.refractiveIndex1)
            assertEquals(refractiveIndexes2[i], computedHit.refractiveIndex2)
        }
    }

    @Test
    fun `under point is below surface`() {
        val shape = Sphere().apply {
            material = Material.GLASS
            transformation = Transformations.translation(0, 0, 1)
        }
        val intersection = Intersection(5, shape)
        val intersections = Intersections(intersection)
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val computedHit = intersection.prepareComputations(ray, intersections)
        assertTrue(computedHit.underPoint.z > (EPSILON / 2.0))
        assertTrue(computedHit.underPoint.z > computedHit.point.z)
    }
}
