package org.example.ktracer.composites

import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Sphere
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.ktracer.coarseEquals

class ComputedHitTest {
    @Test
    fun `schlick approximation under total internal reflection`() {
        val shape = Sphere().apply {
            material = Material.GLASS
        }
        val ray = Ray(Point(0, 0, sqrt(2.0) / 2.0), Vector.UP)
        val intersections = Intersections(Intersection(-sqrt(2.0) / 2.0, shape), Intersection(sqrt(2.0) / 2.0, shape))
        val computedHit = intersections[1].prepareComputations(ray, intersections)
        assertEquals(1.0, computedHit.schlick())
    }

    @Test
    fun `schlick approximation perpendicular to viewing angle`() {
        val shape = Sphere().apply {
            material = Material.GLASS
        }
        val ray = Ray(Point.ORIGIN, Vector.UP)
        val intersections = Intersections(Intersection(-1, shape), Intersection(1, shape))
        val computedHit = intersections[1].prepareComputations(ray, intersections)
        assertEquals(0.04000000000000001, computedHit.schlick())
    }

    @Test
    fun `schlick approximation with small angle`() {
        val shape = Sphere().apply {
            material = Material.GLASS
        }
        val ray = Ray(Point(0, 0.99, -2), Vector.FORWARD)
        val intersections = Intersections(Intersection(1.8589, shape))
        val computedHit = intersections[0].prepareComputations(ray, intersections)
        assertTrue(0.48873081012212183 coarseEquals computedHit.schlick())
    }
}
