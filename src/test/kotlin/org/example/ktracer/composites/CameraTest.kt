package org.example.ktracer.composites

import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.ktracer.primitives.Matrix
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector

class CameraTest {
    @Test
    fun `constructing camera`() {
        val horizontalSize = 160
        val verticalSize = 120
        val fieldOfView = (PI / 2.0).toDouble()
        val camera = Camera(horizontalSize, verticalSize, fieldOfView)
        assertEquals(horizontalSize, camera.horizontalSize)
        assertEquals(verticalSize, camera.verticalSize)
        assertEquals(fieldOfView, camera.fieldOfView)
        assertEquals(Matrix.IDENTITY, camera.transformation)
    }

    @Test
    fun `pixel size for horizontal canvas`() {
        val camera = Camera(200, 125, PI / 2.0)
        assertEquals(0.009999999999999998, camera.pixelSize)
    }

    @Test
    fun `pixel size for vertical canvas`() {
        val camera = Camera(125, 200, PI / 2.0)
        assertEquals(0.009999999999999998, camera.pixelSize)
    }

    @Test
    fun `ray through canvas center`() {
        val camera = Camera(201, 101, PI / 2.0)
        val ray = camera.rayForPixel(100, 50)
        assertEquals(Point.ORIGIN, ray.origin)
        assertEquals(Vector.BACKWARD, ray.direction)
    }

    @Test
    fun `ray through canvas corner`() {
        val camera = Camera(201, 101, PI / 2.0)
        val ray = camera.rayForPixel(0, 0)
        assertEquals(Point.ORIGIN, ray.origin)
        assertEquals(Vector(0.6651864261194508, 0.3325932130597254, -0.6685123582500481), ray.direction)
    }

    @Test
    fun `ray through canvas with transformed camera`() {
        val camera = Camera(201, 101, PI / 2.0)
        camera.transformation = Transformations.rotationY(PI / 4.0) * Transformations.translation(0, -2, 5)
        val ray = camera.rayForPixel(100, 50)
        assertEquals(Point(0, 2, -5), ray.origin)
        assertEquals(Vector(sqrt(2.0) / 2.0, 0, -sqrt(2.0) / 2.0), ray.direction)
    }
}
