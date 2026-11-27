package org.example.ktracer.colorproviders.patterns

import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.colorproviders.toPatternSpace
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Transformable

/** A solid 3D pattern: a color for every point in pattern space. Implementations are immutable. */
abstract value class Pattern : Transformable, ColorProvider {
    abstract fun colorAt(point: Point): Color

    override fun colorAtShape(shape: Shape, point: Point): Color = colorAt(toPatternSpace(shape, point))
}
