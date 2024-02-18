package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point

class TestPattern : Pattern() {
    override fun colorAt(point: Point): Color {
        return Color(point.x, point.y, point.z)
    }

    override fun toString(): String {
        return "TestPattern"
    }
}
