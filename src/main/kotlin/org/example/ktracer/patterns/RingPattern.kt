package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.squared
import kotlin.math.floor
import kotlin.math.sqrt

data class RingPattern(val colorA: Color, val colorB: Color) : Pattern() {
    override fun colorAt(point: Point): Color {
        val distance = (point.x.squared() + point.z.squared()).run(::sqrt).run(::floor)
        return if (distance % 2.0 == 0.0) colorA else colorB
    }

    override fun toString(): String {
        return "RingPattern(colorA=$colorA, colorB=$colorB)"
    }
}
