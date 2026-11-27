package org.example.ktracer.colorproviders.patterns

import kotlin.math.floor
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

value class StripePattern(
    val colorA: Color,
    val colorB: Color,
    override val transformation: Transformation = Transformation.identity(),
) : Pattern() {
    override fun colorAt(point: Point): Color {
        val distance = floor(point.x)
        return if (distance % 2.0 == 0.0) colorA else colorB
    }
}
