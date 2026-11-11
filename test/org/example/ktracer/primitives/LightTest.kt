package org.example.ktracer.primitives

import kotlin.test.Test
import kotlin.test.assertEquals

class LightTest {
    @Test
    fun `creating and inspecting light`() {
        val position = Point.ORIGIN
        val intensity = Color.WHITE
        val light = Light(position, intensity)
        assertEquals(position, light.position)
        assertEquals(intensity, light.intensity)
    }
}
