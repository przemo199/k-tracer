package org.example.ktracer.colorproviders

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Transformable

/** Colors a shape's surface: a 3D [patterns.Pattern], or a texture ([textures.TextureMap], [textures.CubeMap]). */
interface ColorProvider {
    /** The color at [point], a point in world space on [shape]. */
    fun colorAtShape(shape: Shape, point: Point): Color
}

/**
 * [point] (in world space, on [shape]) in this color provider's own space: through the shape's transformations,
 * including any parent groups, then this provider's [Transformable.transformation].
 */
fun Transformable.toPatternSpace(shape: Shape, point: Point): Point =
    transformation.inverse() * shape.worldToObject(point)
