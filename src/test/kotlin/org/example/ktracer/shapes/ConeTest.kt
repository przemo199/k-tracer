package org.example.ktracer.shapes

import java.util.stream.Stream
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.ktracer.MAX
import org.example.ktracer.MIN
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class ConeTest {
    private val cone = Cone()

    @Test
    fun `default cone`() {
        assertEquals(MIN, cone.min)
        assertEquals(MAX, cone.max)
        assertTrue(!cone.closed)
    }

    @ParameterizedTest
    @MethodSource("intersecting ray with cone data")
    fun `intersecting ray with cone`(origin: Point, direction: Vector, distance1: Double, distance2: Double) {
        val ray = Ray(origin, direction.normalized())
        val intersections = Intersections()
        cone.localIntersect(ray, intersections)
        assertEquals(2, intersections.size)
        assertEquals(distance1, intersections[0].distance)
        assertEquals(distance2, intersections[1].distance)
    }

    @Test
    fun `intersecting ray with cone parallel to one of cone halves`() {
        val ray = Ray(Point(0, 0, -1), Vector(0, 1, 1).normalized())
        val intersections = Intersections()
        cone.localIntersect(ray, intersections)
        assertEquals(1, intersections.size)
        assertEquals(0.3535533905932738, intersections[0].distance)
    }

    @ParameterizedTest
    @MethodSource("intersecting ray with cone caps data")
    fun `intersecting ray with cone caps`(origin: Point, direction: Vector, count: Int) {
        val cone = Cone(-0.5, 0.5, true)
        val ray = Ray(origin, direction.normalized())
        val intersections = Intersections()
        cone.localIntersect(ray, intersections)
        assertEquals(count, intersections.size)
    }

    @ParameterizedTest
    @MethodSource("computing normal vector on cone data")
    fun `computing normal vector on cone`(point: Point, expectedNormal: Vector) {
        val normal = cone.localNormalAt(point)
        assertEquals(expectedNormal, normal)
    }

    @Test
    fun `unbounded cone has bounding box`() {
        val boundingBox = cone.boundingBox()
        assertEquals(Point(MIN, MIN, MIN), boundingBox.min)
        assertEquals(Point(MAX, MAX, MAX), boundingBox.max)
    }

    @Test
    fun `bounded cone has bounding box`() {
        val cone = Cone(-5, 3)
        val boundingBox = cone.boundingBox()
        assertEquals(Point(-5, -5, -5), boundingBox.min)
        assertEquals(Point(5, 3, 5), boundingBox.max)
    }

    companion object {
        @JvmStatic
        fun `intersecting ray with cone data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(0, 0, -5), Vector.FORWARD, 5.0, 5.0),
                arguments(Point(0, 0, -5.01), Vector(1, 1, 1), 8.677574545920073, 8.677574545920073),
                arguments(Point(1, 1, -5), Vector(-0.5, -1, 1), 4.550055679356349, 49.449944320643645),
            )
        }

        @JvmStatic
        fun `intersecting ray with cone caps data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(0, 0, -5), Vector.UP, 0),
                arguments(Point(0, 0, -0.25), Vector(0, 1, 1), 2),
                arguments(Point(0, 0, -0.25), Vector.UP, 4),
            )
        }

        @JvmStatic
        fun `computing normal vector on cone data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point.ORIGIN, Vector.ZERO),
                arguments(Point(1, 1, 1), Vector(1, -sqrt(2.0), 1)),
                arguments(Point(-1, -1, 0), Vector(-1, 1, 0)),
            )
        }
    }
}
