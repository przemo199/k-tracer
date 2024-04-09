package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import kotlin.math.absoluteValue

data class GradientPattern(val colorA: Color, val colorB: Color) : Pattern() {
    override fun colorAt(point: Point): Color {
        val distance = colorB - colorA
        var fraction = (point.x % 1.0).absoluteValue
        if (point.x.toInt() % 2 != 0) {
            fraction = 1.0 - fraction
        }
        return colorA + (distance * fraction)
    }

    override fun toString(): String {
        return "GradientPattern(colorA=${colorA}, colorB=${colorB})"
    }
}
