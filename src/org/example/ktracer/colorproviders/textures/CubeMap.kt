package org.example.ktracer.colorproviders.textures

import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.colorproviders.mappings.CubeFace
import org.example.ktracer.colorproviders.toPatternSpace
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Transformable

/** A different [UvPattern] on each face of a cube, such as the six images of a skybox. */
value class CubeMap(
    val left: UvPattern,
    val front: UvPattern,
    val right: UvPattern,
    val back: UvPattern,
    val up: UvPattern,
    val down: UvPattern,
    override val transformation: Transformation = Transformation.identity(),
) : Transformable, ColorProvider {
    fun face(face: CubeFace): UvPattern = when (face) {
        CubeFace.LEFT -> left
        CubeFace.FRONT -> front
        CubeFace.RIGHT -> right
        CubeFace.BACK -> back
        CubeFace.UP -> up
        CubeFace.DOWN -> down
    }

    override fun colorAtShape(shape: Shape, point: Point): Color {
        val patternPoint = toPatternSpace(shape, point)
        val face = CubeFace.of(patternPoint)
        return face(face).colorAt(face.coordinates(patternPoint))
    }
}
