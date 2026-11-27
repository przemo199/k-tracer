package org.example.ktracer.colorproviders.textures

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates

/**
 * The book's test pattern for checking a mapping's orientation: one color with a differently colored square in each
 * corner (each a fifth of the width and height). If the corners land where expected, `u` and `v` run the right way.
 */
value class UvAlignCheck(
    val main: Color,
    val upperLeft: Color,
    val upperRight: Color,
    val bottomLeft: Color,
    val bottomRight: Color,
) : UvPattern {
    override fun colorAt(coordinates: Coordinates): Color {
        val left = coordinates.u < 0.2
        val right = coordinates.u > 0.8
        val upper = coordinates.v > 0.8
        val bottom = coordinates.v < 0.2
        return when {
            upper && left -> upperLeft
            upper && right -> upperRight
            bottom && left -> bottomLeft
            bottom && right -> bottomRight
            else -> main
        }
    }
}
