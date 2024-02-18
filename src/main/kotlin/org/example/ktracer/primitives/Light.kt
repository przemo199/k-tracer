package org.example.ktracer.primitives

data class Light(val position: Point = DEFAULT_LIGHT_POSITION, val intensity: Color = Color.WHITE) {
    companion object {
        @JvmField
        val DEFAULT_LIGHT_POSITION = Point(-10, 10, -10)
    }
}
