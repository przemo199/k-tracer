package org.example.ktracer.colorproviders.textures

import org.example.ktracer.colorproviders.mappings.Mapping
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Transformation

class CheckeredTexture(
    val colorA: Color,
    val colorB: Color,
    val width: Int,
    val height: Int,
    mapping: Mapping,
    transformation: Transformation = Transformation.identity()
) : Texture(mapping, transformation) {

    override fun colorAt(coordinates: Coordinates): Color {
        val u2 = (coordinates.u * width).toInt()
        val v2 = (coordinates.v * height).toInt()

        return if ((u2 + v2) % 2 == 0) colorA else colorB
    }

    override fun toString(): String {
        return "CheckeredTexture(colorA=$colorA, colorB=$colorB, width=$width, height=$height)"
    }
}