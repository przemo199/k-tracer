package org.example.ktracer.colorproviders.patterns

import kotlin.math.floor
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

class StripePattern(
    val colorA: Color,
    val colorB: Color,
    transformation: Transformation = Transformation.identity()
) : Pattern(transformation) {
    override fun colorAt(point: Point): Color {
        val distance = floor(point.x)
        return if (distance % 2.0 == 0.0) colorA else colorB
    }

    override fun toString(): String = "StripePattern(colorA=$colorA, colorB=$colorB)"
}
