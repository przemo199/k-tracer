package org.example.ktracer.colorproviders

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.shapes.Shape

interface ColorProvider {
    fun colorAtShape(shape: Shape, point: Point): Color
}
