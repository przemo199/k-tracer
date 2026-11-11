package org.example.ktracer.colorproviders.patterns

import kotlin.math.floor
import kotlin.math.sqrt
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.squared

class RingPattern(
    val colorA: Color,
    val colorB: Color,
    transformation: Transformation = Transformation.identity()
) : Pattern(transformation) {
    override fun colorAt(point: Point): Color {
        val distance = (point.x.squared() + point.z.squared()).run(::sqrt).run(::floor)
        return if (distance % 2.0 == 0.0) colorA else colorB
    }

    override fun toString(): String = "RingPattern(colorA=$colorA, colorB=$colorB)"
}
