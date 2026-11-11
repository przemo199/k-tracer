package org.example.ktracer.colorproviders.patterns

import kotlin.math.floor
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

class ComplexPattern(
    val patternA: Pattern,
    val patternB: Pattern,
    transformation: Transformation = Transformation.identity()
) : Pattern(transformation) {
    override fun colorAt(point: Point): Color {
        val distance = floor(point.x)
        return if (distance % 2.0 == 0.0) patternA.colorAt(point) else patternB.colorAt(point)
    }

    override fun toString(): String = "ComplexPattern(patternA=$patternA, patternB=$patternB)"
}
