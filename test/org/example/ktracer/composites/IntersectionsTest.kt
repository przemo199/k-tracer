package org.example.ktracer.composites

import org.example.ktracer.shapes.Sphere
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IntersectionsTest {
    @Test
    fun `hit when all intersections are positive`() {
        val shape = Sphere()
        val intersections = Intersections(Intersection(1.0, shape), Intersection(2.0, shape))
        assertEquals(intersections[0], intersections.hit())
    }

    @Test
    fun `hit when some intersections are negative`() {
        val shape = Sphere()
        val intersections = Intersections(Intersection(-1.0, shape), Intersection(1.0, shape))
        assertEquals(intersections[1], intersections.hit())
    }

    @Test
    fun `hit when all intersections are negative`() {
        val shape = Sphere()
        val intersections = Intersections(Intersection(-2.0, shape), Intersection(-1.0, shape))
        assertNull(intersections.hit())
    }

    @Test
    fun `hit is always lowest non-negative`() {
        val shape = Sphere()
        val intersections = Intersections(
            Intersection(5.0, shape),
            Intersection(7.0, shape),
            Intersection(-3.0, shape),
            Intersection(2.0, shape),
        )
        assertEquals(intersections[3], intersections.hit())
    }
}
