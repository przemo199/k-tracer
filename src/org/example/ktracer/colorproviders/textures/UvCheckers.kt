package org.example.ktracer.colorproviders.textures

import kotlin.math.floor
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates

/** Checkers in texture space: [width] × [height] squares across the `0..1` range of `u` and `v`. */
data class UvCheckers(val width: Int, val height: Int, val colorA: Color, val colorB: Color) : UvPattern {
    init {
        require(width > 0 && height > 0) { "checkers need a positive width and height" }
    }

    override fun colorAt(coordinates: Coordinates): Color {
        // `floor`, not `toInt` (which rounds toward zero), so squares stay regular below zero
        val u = floor(coordinates.u * width).toLong()
        val v = floor(coordinates.v * height).toLong()
        return if ((u + v).mod(2L) == 0L) colorA else colorB
    }
}
