package org.example.ktracer.shapes

import org.example.ktracer.composites.Material
import org.example.ktracer.primitives.Matrix
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class SphereTest {
    @Test
    fun `default sphere`() {
        val sphere = Sphere()
        assertEquals(Matrix.IDENTITY, sphere.transformation)
        assertEquals(Material(), sphere.material)
    }

    @Test
    fun `sphere normal`() {
        val sphere = Sphere()
        val normal = sphere.normalAt(Point(1, 0, 0))
        assertEquals(Vector(1, 0, 0), normal)
        val normal2 = sphere.normalAt(Point(0, 1, 0))
        assertEquals(Vector(0, 1, 0), normal2)
        val normal3 = sphere.normalAt(Point(0, 0, 1))
        assertEquals(Vector(0, 0, 1), normal3)
        val thirdOfSqrt3 = sqrt(3.0) / 3.0
        val normal4 = sphere.normalAt(Point(thirdOfSqrt3, thirdOfSqrt3, thirdOfSqrt3))
        assertEquals(Vector(thirdOfSqrt3, thirdOfSqrt3, thirdOfSqrt3), normal4)
    }

    @Test
    fun `sphere normal is normalized`() {
        val sphere = Sphere()
        val thirdOfSqrt3 = sqrt(3.0) / 3.0
        val normal = sphere.normalAt(Point(thirdOfSqrt3, thirdOfSqrt3, thirdOfSqrt3))
        assertEquals(normal, normal.normalized())
    }

    @Test
    fun `normal on a translated sphere`() {
        val sphere = Sphere()
        sphere.transformation = Transformations.scaling(1, 0.5, 1) * Transformations.rotationZ(PI / 5.0)
        val normal = sphere.normalAt(Point(0.0, sqrt(2.0) / 2.0, -sqrt(2.0) / 2.0))
        assertEquals(Vector(0, 0.9701425001453319, -0.24253562503633294), normal)
    }

    @Test
    fun `normal on a transformed sphere`() {
        val sphere = Sphere()
        sphere.transformation = Transformations.translation(0, 1, 0)
        val normal = sphere.normalAt(Point(0.0, 1.70711, -0.70711))
        assertEquals(Vector(0, 0.7071067811865475, -0.7071067811865476), normal)
    }

    @Test
    fun `sphere has bounding box`() {
        val sphere = Sphere()
        val boundingBox = sphere.boundingBox()
        assertEquals(Point(-1, -1, -1), boundingBox.min)
        assertEquals(Point(1, 1, 1), boundingBox.max)
    }
}
