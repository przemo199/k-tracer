package org.example.ktracer.colorproviders.patterns

import kotlin.math.floor
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

/**
 * The book's nested patterns: alternates between [patternA] and [patternB] along x. Each is evaluated in its own
 * pattern space, so the two can be transformed independently.
 */
value class ComplexPattern(
    val patternA: Pattern,
    val patternB: Pattern,
    override val transformation: Transformation = Transformation.identity(),
) : Pattern() {
    override fun colorAt(point: Point): Color {
        val pattern = if (floor(point.x) % 2.0 == 0.0) patternA else patternB
        return pattern.colorAt(pattern.transformation.inverse() * point)
    }
}
