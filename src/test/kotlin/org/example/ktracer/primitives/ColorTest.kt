package org.example.ktracer.primitives

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ColorTest {

    @Test
    fun `colors are (red, green, blue) tuples`() {
        val color = Color(-0.5, 0.4, 1.7)
        assertEquals(-0.5, color.red)
        assertEquals(0.4, color.green)
        assertEquals(1.7, color.blue)
        assertEquals(-0.5, color.x)
        assertEquals(0.4, color.y)
        assertEquals(1.7, color.z)
    }

    @Test
    fun `white color exists`() {
        assertEquals(Color(1.0, 1.0, 1.0), Color.WHITE)
    }

    @Test
    fun `black color exists`() {
        assertEquals(Color(0.0, 0.0, 0.0), Color.BLACK)
    }

    @Test
    fun `red color exists`() {
        assertEquals(Color(1.0, 0.0, 0.0), Color.RED)
    }

    @Test
    fun `green color exists`() {
        assertEquals(Color(0.0, 1.0, 0.0), Color.GREEN)
    }

    @Test
    fun `blue color exists`() {
        assertEquals(Color(0.0, 0.0, 1.0), Color.BLUE)
    }

    @Test
    fun `adding colors`() {
        val color1 = Color(0.9, 0.6, 0.75)
        val color2 = Color(0.7, 0.1, 0.25)
        assertEquals(Color(1.6, 0.7, 1.0), color1 + color2)
    }

    @Test
    fun `subtracting colors`() {
        val color1 = Color(0.9, 0.6, 0.75)
        val color2 = Color(0.7, 0.1, 0.25)
        assertEquals(Color(0.2, 0.5, 0.5), color1 - color2)
    }

    @Test
    fun `multiplying color by a scalar`() {
        val color1 = Color(0.2, 0.3, 0.4)
        assertEquals(Color(0.4, 0.6, 0.8), color1 * 2.0)
    }

    @Test
    fun `multiplying colors`() {
        val color1 = Color(1.0, 0.2, 0.4)
        val color2 = Color(0.9, 1.0, 0.1)
        assertEquals(Color(0.9, 0.2, 0.04), color1 * color2)
    }

    @Test
    fun `comparing colors`() {
        val color1 = Color.WHITE
        val color2 = Color.WHITE
        val color3 = Color.BLACK
        assertEquals(color1, color2)
        assertNotEquals(color1, color3)
    }
}
