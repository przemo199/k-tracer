package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import kotlin.math.floor

data class ComplexPattern(val patternA: Pattern, val patternB: Pattern) : Pattern() {
    override fun colorAt(point: Point): Color {
        val distance = floor(point.x)
        return if (distance % 2.0 == 0.0) patternA.colorAt(point) else patternB.colorAt(point)
    }
}
