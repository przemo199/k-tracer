package org.example.ktracer.composites

import org.example.ktracer.primitives.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class CanvasTest {
    @Test
    fun `creating canvas`() {
        val canvas = Canvas(10, 20)
        assertEquals(10, canvas.width)
        assertEquals(20, canvas.height)
        for (pixel in canvas) {
            assertEquals(Color.BLACK, pixel)
        }
    }

    @Test
    fun `writing pixel canvas`() {
        val canvas = Canvas(10, 20)
        val red = Color.BLACK
        canvas[2, 3] = red
        assertEquals(red, canvas[2, 3])
    }
}
