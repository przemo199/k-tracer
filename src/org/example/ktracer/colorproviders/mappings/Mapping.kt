package org.example.ktracer.colorproviders.mappings

import kotlin.math.PI
import kotlin.math.atan2
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point

/**
 * Maps a point on a shape (in pattern space) to `(u, v)` texture coordinates in `0..1`, which a
 * [org.example.ktracer.colorproviders.textures.UvPattern] turns into a color. Combined in a
 * [org.example.ktracer.colorproviders.textures.TextureMap].
 */
fun interface Mapping {
    fun map(point: Point): Coordinates
}

/**
 * `u` for mappings that wrap around the y-axis: 0 at `-z`, increasing counterclockwise when viewed from above
 * (0.25 at `+x`, 0.5 at `+z`, 0.75 at `-x`).
 */
internal fun aroundYAxis(point: Point): Double {
    // azimuthal angle, in -π..π
    val theta = atan2(point.x, point.z)
    val rawU = theta / (2 * PI)
    return 1 - (rawU + 0.5)
}
