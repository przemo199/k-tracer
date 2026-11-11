package org.example.ktracer.colorproviders.patterns

import kotlin.math.absoluteValue
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

class GradientPattern(
    val colorA: Color,
    val colorB: Color,
    transformation: Transformation = Transformation.identity()
) : Pattern(transformation) {
    override fun colorAt(point: Point): Color {
        val distance = colorB - colorA
        var fraction = (point.x % 1.0).absoluteValue
        if (point.x.toInt() % 2 != 0) {
            fraction = 1.0 - fraction
        }
        return colorA + (distance * fraction)
    }

    override fun toString(): String = "GradientPattern(colorA=$colorA, colorB=$colorB)"
}
