package org.example.ktracer.shapes

import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations.translation
import org.example.ktracer.primitives.Vector
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructiveSolidGeometryTest {
    @Test
    fun `constructing and inspecting csg`() {
        val sphere = Sphere()
        val cube = Cube()
        val csg = ConstructiveSolidGeometry(ConstructiveSolidGeometry.Operand.UNION, sphere, cube)
        assertEquals(ConstructiveSolidGeometry.Operand.UNION, csg.operand)
        assertEquals(sphere, csg.left)
        assertEquals(cube, csg.right)
    }

    @ParameterizedTest
    @MethodSource("evaluating csg union operation data")
    fun `evaluating csg union operation`(leftHit: Boolean, inLeft: Boolean, inRight: Boolean, expected: Boolean) {
        val result = ConstructiveSolidGeometry.intersectionAllowed(ConstructiveSolidGeometry.Operand.UNION, leftHit, inLeft, inRight)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("evaluating csg intersection operation data")
    fun `evaluating csg intersection operation`(leftHit: Boolean, inLeft: Boolean, inRight: Boolean, expected: Boolean) {
        val result = ConstructiveSolidGeometry.intersectionAllowed(ConstructiveSolidGeometry.Operand.INTERSECTION, leftHit, inLeft, inRight)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("evaluating csg difference operation data")
    fun `evaluating csg difference operation`(leftHit: Boolean, inLeft: Boolean, inRight: Boolean, expected: Boolean) {
        val result = ConstructiveSolidGeometry.intersectionAllowed(ConstructiveSolidGeometry.Operand.DIFFERENCE, leftHit, inLeft, inRight)
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("filtering intersections data")
    fun `filtering intersections`(operation: ConstructiveSolidGeometry.Operand, index0: Int, index1: Int) {
        val sphere = Sphere()
        val cube = Cube()
        val csg = ConstructiveSolidGeometry(operation, sphere, cube)
        val intersections = Intersections(Intersection(0, sphere), Intersection(1, cube), Intersection(2, sphere), Intersection(3, cube))
        val result: Intersections = csg.filterIntersections(intersections)
        assertEquals(2, result.size)
        assertEquals(intersections[index0], result[0])
        assertEquals(intersections[index1], result[1])
    }

    @Test
    fun `ray misses csg`() {
        val csg = ConstructiveSolidGeometry(ConstructiveSolidGeometry.Operand.UNION, Sphere(), Cube())
        val ray = Ray(Point(0, 2, -5), Vector(0, 0, 1))
        val intersections = Intersections()
        csg.localIntersect(ray, intersections)
        assertTrue(intersections.isEmpty())
    }

    @Test
    fun `ray hits csg`() {
        val sphere1 = Sphere()
        val sphere2 = Sphere()
        sphere2.transformation = translation(0, 0, 0.5)
        val csg = ConstructiveSolidGeometry(ConstructiveSolidGeometry.Operand.UNION, sphere1, sphere2)
        val ray = Ray(Point(0, 0, -5), Vector(0, 0, 1))
        val intersections = Intersections()
        csg.localIntersect(ray, intersections)
        assertEquals(2, intersections.size)
        assertEquals(4.0, intersections[0].distance)
        assertEquals(sphere1, intersections[0].shape)
        assertEquals(6.5, intersections[1].distance)
        assertEquals(sphere2, intersections[1].shape)
    }

    companion object {
        @JvmStatic
        fun `evaluating csg union operation data`(): Stream<Arguments> {
            return Stream.of(
                arguments(true, true, true, false),
                arguments(true, true, false, true),
                arguments(true, false, true, false),
                arguments(true, false, false, true),
                arguments(false, true, true, false),
                arguments(false, true, false, false),
                arguments(false, false, true, true),
                arguments(false, false, false, true),
            )
        }

        @JvmStatic
        fun `evaluating csg intersection operation data`(): Stream<Arguments> {
            return Stream.of(
                arguments(true, true, true, true),
                arguments(true, true, false, false),
                arguments(true, false, true, true),
                arguments(true, false, false, false),
                arguments(false, true, true, true),
                arguments(false, true, false, true),
                arguments(false, false, true, false),
                arguments(false, false, false, false),
            )
        }

        @JvmStatic
        fun `evaluating csg difference operation data`(): Stream<Arguments> {
            return Stream.of(
                arguments(true, true, true, false),
                arguments(true, true, false, true),
                arguments(true, false, true, false),
                arguments(true, false, false, true),
                arguments(false, true, true, true),
                arguments(false, true, false, true),
                arguments(false, false, true, false),
                arguments(false, false, false, false),
            )
        }

        @JvmStatic
        fun `filtering intersections data`(): Stream<Arguments> {
            return Stream.of(
                arguments(ConstructiveSolidGeometry.Operand.UNION, 0, 3),
                arguments(ConstructiveSolidGeometry.Operand.INTERSECTION, 1, 2),
                arguments(ConstructiveSolidGeometry.Operand.DIFFERENCE, 0, 1),
            )
        }
    }
}
