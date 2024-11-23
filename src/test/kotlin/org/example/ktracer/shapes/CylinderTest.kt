package org.example.ktracer.shapes

import java.util.stream.Stream
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
import org.junit.jupiter.params.provider.MethodSource

class CylinderTest {
    private val cylinder = Cylinder()

    @Test
    fun `default cylinder`() {
        assertEquals(MIN, cylinder.min)
        assertEquals(MAX, cylinder.max)
        assertEquals(false, cylinder.closed)
    }

    @ParameterizedTest
    @MethodSource("ray misses cylinder data")
    fun `ray misses cylinder`(origin: Point, direction: Vector) {
        val ray = Ray(origin, direction.normalized())
        val intersections = Intersections()
        cylinder.localIntersect(ray, intersections)
        assertTrue(intersections.isEmpty())
    }

    @ParameterizedTest
    @MethodSource("ray intersects cylinder data")
    fun `ray intersects cylinder`(origin: Point, direction: Vector, distance1: Double, distance2: Double) {
        val ray = Ray(origin, direction.normalized())
        val intersections = Intersections()
        cylinder.localIntersect(ray, intersections)
        assertEquals(2, intersections.size)
        assertEquals(distance1, intersections[0].distance)
        assertEquals(distance2, intersections[1].distance)
    }

    @ParameterizedTest
    @MethodSource("normal vector on cylinder data")
    fun `normal vector on cylinder`(point: Point, expectedNormal: Vector) {
        val normal = cylinder.localNormalAt(point)
        assertEquals(expectedNormal, normal)
    }

    @ParameterizedTest
    @MethodSource("intersecting constrained cylinder data")
    fun `intersecting constrained cylinder`(origin: Point, direction: Vector, count: Int) {
        val cylinder = Cylinder(min = 1, max = 2)
        val ray = Ray(origin, direction.normalized())
        val intersections = Intersections()
        cylinder.localIntersect(ray, intersections)
        assertEquals(count, intersections.size)
    }

    @Test
    fun `unbounded cylinder has bounding box`() {
        val boundingBox = cylinder.boundingBox()
        assertEquals(Point(-1, MIN, -1), boundingBox.min)
        assertEquals(Point(1, MAX, 1), boundingBox.max)
    }

    @Test
    fun `bounded cylinder has bounding box`() {
        val cylinder = Cylinder(-5, 3)
        val boundingBox = cylinder.boundingBox()
        assertEquals(Point(-1, -5, -1), boundingBox.min)
        assertEquals(Point(1, 3, 1), boundingBox.max)
    }

    companion object {
        @JvmStatic
        fun `ray misses cylinder data`(): Stream<Arguments> {
            return Stream.of(
                Arguments.arguments(Point(1, 0, 0), Vector.UP),
                Arguments.arguments(Point(0, 1, 0), Vector.UP),
                Arguments.arguments(Point(0, 0, -5), Vector(1, 1, 1)),
            )
        }

        @JvmStatic
        fun `ray intersects cylinder data`(): Stream<Arguments> {
            return Stream.of(
                Arguments.arguments(Point(1, 0, -5), Vector.FORWARD, 5.0, 5.0),
                Arguments.arguments(Point(0, 0, -5), Vector.FORWARD, 4.0, 6.0),
                Arguments.arguments(Point(0.5, 0, -5), Vector(0.1, 1, 1), 6.80798191702732, 7.088723439378861),
            )
        }

        @JvmStatic
        fun `normal vector on cylinder data`(): Stream<Arguments> {
            return Stream.of(
                Arguments.arguments(Point(1, 0, 0), Vector.RIGHT),
                Arguments.arguments(Point(0, 5, -1), Vector.BACKWARD),
                Arguments.arguments(Point(0, -2, 1), Vector.FORWARD),
                Arguments.arguments(Point(-1, 1, 0), Vector.LEFT),
            )
        }

        @JvmStatic
        fun `intersecting constrained cylinder data`(): Stream<Arguments> {
            return Stream.of(
                Arguments.arguments(Point(0, 1.5, 0), Vector(0.1, 1, 0), 0),
                Arguments.arguments(Point(0, 3, -5), Vector.FORWARD, 0),
                Arguments.arguments(Point(0, 0, -5), Vector.FORWARD, 0),
                Arguments.arguments(Point(0, 2, -5), Vector.FORWARD, 0),
                Arguments.arguments(Point(0, 1, -5), Vector.FORWARD, 0),
                Arguments.arguments(Point(0, 1.5, -2), Vector.FORWARD, 2),
            )
        }
    }
}
