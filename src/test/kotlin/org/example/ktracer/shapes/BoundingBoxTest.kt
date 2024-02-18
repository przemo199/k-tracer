package org.example.ktracer.shapes

import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.ktracer.MAX
import org.example.ktracer.MIN
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Vector

class BoundingBoxTest {
    @Test
    fun `creating empty bounding box`() {
        val boundingBox = BoundingBox()

        assertEquals(MAX, boundingBox.min.x)
        assertEquals(MAX, boundingBox.min.y)
        assertEquals(MAX, boundingBox.min.z)
        assertEquals(MIN, boundingBox.max.x)
        assertEquals(MIN, boundingBox.max.y)
        assertEquals(MIN, boundingBox.max.z)
    }

    @Test
    fun `creating bounding box with volume`() {
        val boundingBox = BoundingBox(Point(-1, -2, -3), Point(3, 2, 1))

        assertEquals(Point(-1, -2, -3), boundingBox.min)
        assertEquals(Point(3, 2, 1), boundingBox.max)
    }

    @Test
    fun `adding points to empty bounding box`() {
        val boundingBox = BoundingBox()
        boundingBox.add(Point(-5, 2, 0))
        boundingBox.add(Point(7, 0, -3))

        assertEquals(Point(-5, 0, -3), boundingBox.min)
        assertEquals(Point(7, 2, 0), boundingBox.max)
    }

    @Test
    fun `adding one bounding box to another`() {
        val boundingBox1 = BoundingBox(Point(-5, -2, 0), Point(7, 4, 4))
        val boundingBox2 = BoundingBox(Point(8, -7, -2), Point(14, 2, 8))
        boundingBox1.add(boundingBox2)

        assertEquals(Point(-5, -7, -2), boundingBox1.min)
        assertEquals(Point(14, 4, 8), boundingBox1.max)
    }

    @ParameterizedTest
    @MethodSource("checking if bounding box contains given point data")
    fun `checking if bounding box contains given point`(point: Point, contains: Boolean) {
        val boundingBox = BoundingBox(Point(5, -2, 0), Point(11, 4, 7))

        assertEquals(contains, point in boundingBox)
    }

    @ParameterizedTest
    @MethodSource("checking if bounding box contains given box data")
    fun `checking if bounding box contains given box`(min: Point, max: Point, contains: Boolean) {
        val boundingBox = BoundingBox(Point(5, -2, 0), Point(11, 4, 7))
        val box = BoundingBox(min, max)

        assertEquals(contains, box in boundingBox)
    }

    @Test
    fun `transforming bounding box`() {
        val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
        val matrix = Transformations.rotationX(PI / 4.0) * Transformations.rotationY(PI / 4.0)
        val box2 = box.transform(matrix)
        val expected = Point(-1.414213562373095, -1.7071067811865475, -1.7071067811865475)

        assertEquals(expected, box2.min)
        assertEquals(-expected, box2.max)
    }

    @ParameterizedTest
    @MethodSource("intersecting ray with bounding box at the origin data")
    fun `intersecting ray with bounding box at the origin`(origin: Point, direction: Vector, result: Boolean) {
        val boundingBox = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
        val ray = Ray(origin, direction.normalized())

        assertEquals(result, boundingBox.intersects(ray))
    }

    @Test
    fun `splitting perfect cube`() {
        val boundingBox = BoundingBox(Point(-1, -4, -5), Point(9, 6, 5))
        val (left, right) = boundingBox.splitBounds()

        assertEquals(Point(-1, -4, -5), left.min)
        assertEquals(Point(4, 6, 5), left.max)
        assertEquals(Point(4, -4, -5), right.min)
        assertEquals(Point(9, 6, 5), right.max)
    }

    @Test
    fun `splitting x-wide box`() {
        val boundingBox = BoundingBox(Point(-1, -2, -3), Point(9, 5.5, 3))
        val (left, right) = boundingBox.splitBounds()

        assertEquals(Point(-1, -2, -3), left.min)
        assertEquals(Point(4, 5.5, 3), left.max)
        assertEquals(Point(4, -2, -3), right.min)
        assertEquals(Point(9, 5.5, 3), right.max)
    }

    @Test
    fun `splitting y-wide box`() {
        val boundingBox = BoundingBox(Point(-1, -2, -3), Point(5, 8, 3))
        val (left, right) = boundingBox.splitBounds()

        assertEquals(Point(-1, -2, -3), left.min)
        assertEquals(Point(5, 3, 3), left.max)
        assertEquals(Point(-1, 3, -3), right.min)
        assertEquals(Point(5, 8, 3), right.max)
    }

    @Test
    fun `splitting z-wide box`() {
        val boundingBox = BoundingBox(Point(-1, -2, -3), Point(5, 3, 7))
        val (left, right) = boundingBox.splitBounds()

        assertEquals(Point(-1, -2, -3), left.min)
        assertEquals(Point(5, 3, 2), left.max)
        assertEquals(Point(-1, -2, 2), right.min)
        assertEquals(Point(5, 3, 7), right.max)
    }

    companion object {
        @JvmStatic
        fun `checking if bounding box contains given point data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(5, -2, 0), true),
                arguments(Point(11, 4, 7), true),
                arguments(Point(8, 1, 3), true),
                arguments(Point(3, 0, 3), false),
                arguments(Point(8, -4, 3), false),
                arguments(Point(8, 1, -1), false),
                arguments(Point(13, 1, 3), false),
                arguments(Point(8, 5, 3), false),
                arguments(Point(8, 1, 8), false),
            )
        }

        @JvmStatic
        fun `checking if bounding box contains given box data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(5, -2, 0), Point(11, 4, 7), true),
                arguments(Point(6, -1, 1), Point(10, 3, 6), true),
                arguments(Point(4, -3, -1), Point(10, 3, 6), false),
                arguments(Point(6, -1, 1), Point(12, 5, 8), false),
            )
        }

        @JvmStatic
        fun `intersecting ray with bounding box at the origin data`(): Stream<Arguments> {
            return Stream.of(
                arguments(Point(5, 0.5, 0), Vector(-1, 0, 0), true),
                arguments(Point(-5, 0.5, 0), Vector(1, 0, 0) , true),
                arguments(Point(0.5, 5, 0), Vector(0, -1, 0), true),
                arguments(Point(0.5, -5, 0), Vector(0, 1, 0) , true),
                arguments(Point(0.5, 0, 5), Vector(0, 0, -1), true),
                arguments(Point(0.5, 0, -5), Vector(0, 0, 1) , true),
                arguments(Point(0, 0.5, 0), Vector(0, 0, 1) , true),
                arguments(Point(-2, 0, 0), Vector(2, 4, 6) , false),
                arguments(Point(0, -2, 0), Vector(6, 2, 4) , false),
                arguments(Point(0, 0, -2), Vector(4, 6, 2) , false),
                arguments(Point(2, 0, 2), Vector(0, 0, -1), false),
                arguments(Point(0, 2, 2), Vector(0, -1, 0), false),
                arguments(Point(2, 2, 0), Vector(-1, 0, 0), false),
            )
        }
    }
}
