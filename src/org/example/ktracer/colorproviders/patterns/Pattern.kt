package org.example.ktracer.colorproviders.patterns

import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Transformable

abstract class Pattern(
    override var transformation: Transformation
) : Transformable, ColorProvider {

    abstract fun colorAt(point: Point): Color

    override fun colorAtShape(shape: Shape, point: Point): Color {
        val shapePoint = shape.transformation.inverse() * point
        val patternPoint = transformation.inverse() * shapePoint
        return colorAt(patternPoint)
    }
}
