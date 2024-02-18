package org.example.ktracer.primitives

import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class TransformationTest {
    @Test
    fun `multiply translation matrix by a point`() {
        val transform = Transformations.translation(5, -3, 2)
        val point = Point(-3, 4, 5)
        assertEquals(Point(2, 1, 7), transform * point)
    }

    @Test
    fun `multiply inverse of a translation matrix by a point`() {
        val transform = Transformations.translation(5, -3, 2).inverse()
        val point = Point(-3, 4, 5)
        assertEquals(Point(-8, 7, 3), transform * point)
    }

    @Test
    fun `multiplying by a translation matrix does not affect vectors`() {
        val transform = Transformations.translation(5, -3, 2).inverse()
        val vector = Vector(-3, 4, 5)
        assertEquals(vector, transform * vector)
    }

    @Test
    fun `scaling matrix applied to a point`() {
        val transform = Transformations.scaling(2, 3, 4)
        val point = Point(-4, 6, 8)
        assertEquals(Point(-8, 18, 32), transform * point)
    }

    @Test
    fun `scaling matrix applied to a vector`() {
        val transform = Transformations.scaling(2, 3, 4)
        val vector = Vector(-4, 6, 8)
        assertEquals(Vector(-8, 18, 32), transform * vector)
    }

    @Test
    fun `inverse of a scaling matrix applied to a point`() {
        val transform = Transformations.scaling(2, 3, 4).inverse()
        val vector = Vector(-4, 6, 8)
        assertEquals(Vector(-2, 2, 2), transform * vector)
    }

    @Test
    fun `reflection is scaling by a negative value`() {
        val transform = Transformations.scaling(-1, 1, 1)
        val point = Point(2, 3, 4)
        assertEquals(Point(-2, 3, 4), transform * point)
    }

    @Test
    fun `rotate a point around the x axis`() {
        val point = Point(0, 1, 0)
        val halfQuarter = Transformations.rotationX(PI / 4)
        val fullQuarter = Transformations.rotationX(PI / 2)
        val halfSqrtOf2 = sqrt(2.0) / 2.0
        assertEquals(Point(0, halfSqrtOf2, halfSqrtOf2), halfQuarter * point)
        assertEquals(Point(0, 0, 1), fullQuarter * point)
    }

    @Test
    fun `inverse of an x-rotation rotates in the opposite direction`() {
        val point = Point(0, 1, 0)
        val halfQuarter = Transformations.rotationX(PI / 4).inverse()
        val halfSqrtOf2 = sqrt(2.0) / 2.0
        assertEquals(Point(0, halfSqrtOf2, -halfSqrtOf2), halfQuarter * point)
    }

    @Test
    fun `rotate a point around the y axis`() {
        val point = Point(0, 0, 1)
        val halfQuarter = Transformations.rotationY(PI / 4)
        val fullQuarter = Transformations.rotationY(PI / 2)
        val halfSqrtOf2 = sqrt(2.0) / 2.0
        assertEquals(Point(halfSqrtOf2, 0, halfSqrtOf2), halfQuarter * point)
        assertEquals(Point(1, 0, 0), fullQuarter * point)
    }

    @Test
    fun `rotate a point around z axis`() {
        val point = Point(0, 1, 0)
        val halfQuarter = Transformations.rotationZ(PI / 4)
        val fullQuarter = Transformations.rotationZ(PI / 2)
        val halfSqrtOf2 = sqrt(2.0) / 2.0
        assertEquals(Point(-halfSqrtOf2, halfSqrtOf2, 0), halfQuarter * point)
        assertEquals(Point(-1, 0, 0), fullQuarter * point)
    }

    @Test
    fun `shearing transformation moves x in relation to y`() {
        val transform = Transformations.shearing(1, 0, 0, 0, 0, 0)
        val point = Point(2, 3, 4)
        assertEquals(Point(5, 3, 4), transform * point)
    }

    @Test
    fun `shearing transformation moves x in relation to z`() {
        val transform = Transformations.shearing(0, 1, 0, 0, 0, 0)
        val point = Point(2, 3, 4)
        assertEquals(Point(6, 3, 4), transform * point)
    }

    @Test
    fun `shearing transformation moves y in relation to x`() {
        val transform = Transformations.shearing(0, 0, 1, 0, 0, 0)
        val point = Point(2, 3, 4)
        assertEquals(Point(2, 5, 4), transform * point)
    }

    @Test
    fun `shearing transformation moves y in relation to z`() {
        val transform = Transformations.shearing(0, 0, 0, 1, 0, 0)
        val point = Point(2, 3, 4)
        assertEquals(Point(2, 7, 4), transform * point)
    }

    @Test
    fun `shearing transformation moves z in relation to x`() {
        val transform = Transformations.shearing(0, 0, 0, 0, 1, 0)
        val point = Point(2, 3, 4)
        assertEquals(Point(2, 3, 6), transform * point)
    }

    @Test
    fun `shearing transformation moves z in relation to y`() {
        val transform = Transformations.shearing(0, 0, 0, 0, 0, 1)
        val point = Point(2, 3, 4)
        assertEquals(Point(2, 3, 7), transform * point)
    }

    @Test
    fun `individual transformations are applied in sequence`() {
        val point = Point(1, 0, 1)
        val rotation = Transformations.rotationX((PI / 2))
        val scaling = Transformations.scaling(5, 5, 5)
        val translation = Transformations.translation(10, 5, 7)
        val point2 = rotation * point
        assertEquals(Point(1, -1, 0), point2)
        val point3 = scaling * point2
        assertEquals(Point(5, -5, 0), point3)
        val point4 = translation * point3
        assertEquals(Point(15, 0, 7), point4)
    }

    @Test
    fun `chained transformations are applied in reverse order`() {
        val point = Point(1, 0, 1)
        val rotation = Transformations.rotationX((PI / 2))
        val scaling = Transformations.scaling(5, 5, 5)
        val translation = Transformations.translation(10, 5, 7)
        val transformation = translation * scaling * rotation
        assertEquals(Point(15, 0, 7), transformation * point)
    }
}
