package org.example.ktracer.colorproviders.mappings

import kotlin.math.absoluteValue
import kotlin.math.max
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point

/** A face of the unit cube (`-1..1` on every axis). [FRONT] is `+z`, [BACK] is `-z`. */
enum class CubeFace {
    LEFT,
    RIGHT,
    FRONT,
    BACK,
    UP,
    DOWN,
    ;

    /**
     * Texture coordinates of a point on this face (the book's convention). Each face shows a whole UV pattern,
     * upright when seen from *inside* the cube, as in a skybox: facing [FRONT] with `+y` up, then turning to each
     * face. Seen from outside, faces appear mirrored.
     */
    fun coordinates(point: Point): Coordinates = when (this) {
        FRONT -> unit(point.x + 1, point.y + 1)
        BACK -> unit(1 - point.x, point.y + 1)
        LEFT -> unit(point.z + 1, point.y + 1)
        RIGHT -> unit(1 - point.z, point.y + 1)
        UP -> unit(point.x + 1, 1 - point.z)
        DOWN -> unit(point.x + 1, point.z + 1)
    }

    companion {
        /** The face a point is on, or nearest to: the axis with the largest magnitude. */
        fun of(point: Point): CubeFace {
            val largest = max(point.x.absoluteValue, max(point.y.absoluteValue, point.z.absoluteValue))
            return when (largest) {
                point.x -> RIGHT
                -point.x -> LEFT
                point.y -> UP
                -point.y -> DOWN
                point.z -> FRONT
                else -> BACK
            }
        }

        /** Maps `0..2` (a face spans -1..1, shifted by 1) to `0..1`, wrapping like the other mappings. */
        private fun unit(u: Double, v: Double) = Coordinates(u.mod(2.0) / 2, v.mod(2.0) / 2)
    }
}
