package org.example.ktracer.colorproviders.patterns

import kotlin.math.absoluteValue
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

/** Blends from [colorA] to [colorB] along x, then back again, so it stays continuous (the book jumps back). */
value class GradientPattern(
    val colorA: Color,
    val colorB: Color,
    override val transformation: Transformation = Transformation.identity(),
) : Pattern() {
    override fun colorAt(point: Point): Color {
        val distance = colorB - colorA
        var fraction = (point.x % 1.0).absoluteValue
        if (point.x.toInt() % 2 != 0) {
            fraction = 1.0 - fraction
        }
        return colorA + (distance * fraction)
    }
}
