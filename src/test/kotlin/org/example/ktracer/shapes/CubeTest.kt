package org.example.ktracer.shapes

import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.ktracer.composites.Intersections

class CubeTest {
    @ParameterizedTest
    @MethodSource("ray intersects cube data")
    fun `ray intersects cube`(origin: Point, direction: Vector, distance1: Double, distance2: Double) {
        val cube = Cube()
        val ray = Ray(origin, direction)
        val intersections = Intersections()
        cube.localIntersect(ray, intersections)
        assertEquals(2, intersections.size)
        assertEquals(distance1, intersections[0].distance)
        assertEquals(distance2, intersections[1].distance)
    }

    @ParameterizedTest
    @MethodSource("ray misses cube data")
    fun `ray misses cube`(origin: Point, direction: Vector) {
        val cube = Cube()
        val ray = Ray(origin, direction)
        val intersections = Intersections()
        cube.localIntersect(ray, intersections)
        assertTrue(intersections.isEmpty())
    }

    @ParameterizedTest
    @MethodSource("normal on surface of cube data")
    fun `normal on surface of cube`(point: Point, expectedNormal: Vector) {
        val cube = Cube()
        val normal = cube.localNormalAt(point)
        assertEquals(expectedNormal, normal)
    }

    @Test
    fun `cube has bounding box`() {
        val cube = Cube()
        val boundingBox = cube.boundingBox()
        assertEquals(Point(-1, -1, -1), boundingBox.min)
        assertEquals(Point(1, 1, 1), boundingBox.max)
    }

    companion object {
        @JvmStatic
        fun `ray intersects cube data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(5, 0.5, 0), Vector.LEFT, 4, 6),
                arguments(Point(-5, 0.5, 0), Vector.RIGHT, 4, 6),
                arguments(Point(0.5, 5, 0), Vector.DOWN, 4, 6),
                arguments(Point(0.5, -5, 0), Vector.UP, 4, 6),
                arguments(Point(0.5, 0, 5), Vector.BACKWARD, 4, 6),
                arguments(Point(0.5, 0, -5), Vector.FORWARD, 4, 6),
                arguments(Point(0, 0.5, 0), Vector.FORWARD, -1, 1),
            )
        }

        @JvmStatic
        fun `ray misses cube data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(-2, 0, 0), Vector(0.2673, 0.5345, 0.8018)),
                arguments(Point(0, -2, 0), Vector(0.8018, 0.2673, 0.5345)),
                arguments(Point(0, 0, -2), Vector(0.5345, 0.8018, 0.2673)),
                arguments(Point(2, 0, 2), Vector.BACKWARD),
                arguments(Point(0, 2, 2), Vector.DOWN),
                arguments(Point(2, 2, 0), Vector.LEFT),
                arguments(Point(0, 0, 2), Vector(0, 0, 1)),
            )
        }

        @JvmStatic
        fun `normal on surface of cube data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(1, 0.5, -0.8), Vector.RIGHT),
                arguments(Point(-1, -0.2, 0.9), Vector.LEFT),
                arguments(Point(-0.4, 1, -0.1), Vector.UP),
                arguments(Point(0.3, -1, -0.7), Vector.DOWN),
                arguments(Point(-0.6, 0.3, 1), Vector.FORWARD),
                arguments(Point(0.4, 0.4, -1), Vector.BACKWARD),
                arguments(Point(1, 1, 1), Vector.RIGHT),
                arguments(Point(-1, -1, -1), Vector.LEFT),
            )
        }
    }
}
