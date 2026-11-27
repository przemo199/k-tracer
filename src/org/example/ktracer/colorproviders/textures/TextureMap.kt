package org.example.ktracer.colorproviders.textures

import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.colorproviders.mappings.Mapping
import org.example.ktracer.colorproviders.toPatternSpace
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Transformable

/**
 * Paints a [UvPattern] onto shapes: a point is taken to pattern space (the shape's and this texture's
 * transformations), turned into `(u, v)` by the [mapping], and colored by the [uvPattern].
 */
value class TextureMap(
    val uvPattern: UvPattern,
    val mapping: Mapping,
    override val transformation: Transformation = Transformation.identity(),
) : Transformable, ColorProvider {
    override fun colorAtShape(shape: Shape, point: Point): Color =
        uvPattern.colorAt(mapping.map(toPatternSpace(shape, point)))
}
