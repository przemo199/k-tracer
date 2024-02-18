package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import kotlin.math.floor

data class CheckerPattern(val colorA: Color, val colorB: Color) : Pattern() {
    override fun colorAt(point: Point): Color {
        val distance = floor(point.x) + floor(point.y) + floor(point.z)
        return if (distance.toInt() % 2 == 0) colorA else colorB
    }
}
