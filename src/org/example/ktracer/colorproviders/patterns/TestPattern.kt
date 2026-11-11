package org.example.ktracer.colorproviders.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

class TestPattern(transformation: Transformation = Transformation.identity()) : Pattern(transformation) {
    override fun colorAt(point: Point): Color = Color(point.x, point.y, point.z)

    override fun toString(): String = "TestPattern"
}
