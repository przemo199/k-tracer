package org.example.ktracer.patterns

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Transformable

abstract class Pattern : Transformable {
    override var transformationInverse: Transformation = Transformation.IDENTITY

    abstract fun colorAt(point: Point): Color

    fun colorAtShape(shape: Shape, point: Point): Color {
        val shapePoint = shape.transformationInverse * point
        val patternPoint = transformationInverse * shapePoint
        return colorAt(patternPoint)
    }

    abstract override fun toString(): String
}
