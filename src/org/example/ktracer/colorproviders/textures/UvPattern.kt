package org.example.ktracer.colorproviders.textures

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates

/**
 * A 2D pattern: a color for each `(u, v)` texture coordinate in `0..1`. Combined with a
 * [org.example.ktracer.colorproviders.mappings.Mapping] in a [TextureMap], any UV pattern works on any shape the
 * mapping supports.
 */
fun interface UvPattern {
    fun colorAt(coordinates: Coordinates): Color
}
