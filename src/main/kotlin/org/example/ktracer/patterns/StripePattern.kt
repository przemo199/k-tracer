package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point

import kotlin.math.floor

data class StripePattern(val colorA: Color, val colorB: Color) : Pattern() {
    override fun colorAt(point: Point): Color {
        val distance = floor(point.x)
        return if (distance % 2.0 == 0.0) colorA else colorB
    }

    override fun toString(): String {
        return "StripePattern(colorA=$colorA, colorB=$colorB)"
    }
}
