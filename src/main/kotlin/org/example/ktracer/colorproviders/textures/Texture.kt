package org.example.ktracer.colorproviders.textures

import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.colorproviders.mappings.Mapping
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Transformable

abstract class Texture(
    var mapping: Mapping,
    override var transformation: Transformation = Transformation.identity()
) : Transformable, ColorProvider {

    abstract fun colorAt(coordinates: Coordinates): Color

    override fun colorAtShape(shape: Shape, point: Point): Color {
        val coordinates = mapping.map(point)
        return colorAt(coordinates)
    }
}
