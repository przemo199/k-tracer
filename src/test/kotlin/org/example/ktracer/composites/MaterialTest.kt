package org.example.ktracer.composites

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Sphere
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MaterialTest {
    @Test
    fun `default material`() {
        val material = Material()
        assertEquals(Color.WHITE, material.color)
        assertEquals(0.1, material.ambient)
        assertEquals(0.9, material.diffuse)
        assertEquals(0.9, material.specular)
        assertEquals(200.0, material.shininess)
        assertEquals(0.0, material.reflectiveness)
        assertEquals(0.0, material.transparency)
        assertEquals(1.0, material.refractiveIndex)
    }

    @Test
    fun `compare materials`() {
        val material1 = Material()
        val material2 = Material()
        assertEquals(material1, material2)
        material2.diffuse = 0.7
        assertNotEquals(material1, material2)
    }

    @Test
    fun `lightning with the camera between light and surface`() {
        val shape = Sphere()
        val position = Point(0, 0, 0)
        val camera = Vector(0, 0, -1)
        val normal = Vector(0, 0, -1)
        val light = Light(Point(0, 0, -10), Color.WHITE)
        val result = shape.material.lighting(shape, light, position, camera, normal, false)
        assertEquals(Color(1.9, 1.9, 1.9), result)
    }

    @Test
    fun `lightning with the camera between light and surface, camera offset 45 degree`() {
        val shape = Sphere()
        val halfSqrtOf2 = sqrt(2.0) / 2.0
        val position = Point(0, 0, 0)
        val camera = Vector(0, halfSqrtOf2, -halfSqrtOf2)
        val normal = Vector(0, 0, -1)
        val light = Light(Point(0, 0, -10), Color.WHITE)
        val result = shape.material.lighting(shape, light, position, camera, normal, false)
        assertEquals(Color(1.0, 1.0, 1.0), result)
    }

    @Test
    fun `lightning with the camera opposite surface, light offset 45 degree`() {
        val shape = Sphere()
        val position = Point(0, 0, 0)
        val camera = Vector(0, 0, -1)
        val normal = Vector(0, 0, -1)
        val light = Light(Point(0, 10, -10), Color.WHITE)
        val result = shape.material.lighting(shape, light, position, camera, normal, false)
        assertEquals(Color(0.7363961, 0.7363961, 0.7363961), result)
    }

    @Test
    fun `lightning with the camera in path of reflection vector`() {
        val shape = Sphere()
        val position = Point.ORIGIN
        val camera = Vector(0, -(sqrt(2.0)) / 2.0, -(sqrt(2.0)) / 2.0)
        val normal = Vector.BACKWARD
        val light = Light(Point(0, 10, -10), Color.WHITE)
        val result = shape.material.lighting(shape, light, position, camera, normal, false)
        assertEquals(Color(1.6363961030678928, 1.6363961030678928, 1.6363961030678928), result)
    }

    @Test
    fun `lightning with light behind surface`() {
        val shape = Sphere()
        val position = Point.ORIGIN
        val camera = Vector.BACKWARD
        val normal = Vector.BACKWARD
        val light = Light(Point(0, 0, 10), Color.WHITE)
        val result = Material().lighting(shape, light, position, camera, normal, false)
        assertEquals(Color(0.1, 0.1, 0.1), result)
    }

    @Test
    fun `lightning with surface in shadow`() {
        val shape = Sphere()
        val position = Point.ORIGIN
        val camera = Vector.BACKWARD
        val normal = Vector.BACKWARD
        val light = Light(Point(0, 0, -10), Color.WHITE)
        val inShadow = true
        val result = Material().lighting(shape, light, position, camera, normal, inShadow)
        assertEquals(Color(0.1, 0.1, 0.1), result)
    }
}
