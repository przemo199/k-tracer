package org.example.ktracer.colorproviders.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

/** Returns its pattern-space point as a color, so tests can check which transformations applied. */
data class TestPattern(override val transformation: Transformation = Transformation.identity()) : Pattern() {
    override fun colorAt(point: Point): Color = Color(point.x, point.y, point.z)
}
