package org.example.ktracer.colorproviders.textures

import com.sksamuel.scrimage.ImmutableImage
import java.nio.file.Path
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates

/**
 * An image used as a texture: `u` runs left to right, `v` from the bottom row (0) to the top (1). Pixels are looked
 * up nearest-neighbor. [pixels] holds `0xRRGGBB` values row by row, starting at the top row.
 */
value class UvImage(val width: Int, val height: Int, private val pixels: IntArray) : UvPattern {
    init {
        require(width > 0 && height > 0) { "texture image must not be empty" }
        require(pixels.size == width * height) { "expected ${width * height} pixels, got ${pixels.size}" }
    }

    override fun colorAt(coordinates: Coordinates): Color {
        // Clamp, so that u, v = 1 (and stray values from a mapping) stay inside the image. Adding 0.5 before toInt
        // rounds to the nearest pixel, and turns NaN (from a mapping's degenerate point) into 0 instead of throwing.
        val x = (coordinates.u.coerceIn(0.0, 1.0) * (width - 1) + 0.5).toInt()
        // image row 0 is the top, while v = 0 is the bottom
        val y = ((1 - coordinates.v.coerceIn(0.0, 1.0)) * (height - 1) + 0.5).toInt()
        val rgb = pixels[y * width + x]
        return Color(channel(rgb shr 16), channel(rgb shr 8), channel(rgb))
    }

    override fun equals(other: Any?): Boolean =
        other is UvImage && width == other.width && height == other.height && pixels.contentEquals(other.pixels)

    override fun hashCode(): Int = 31 * (31 * width + height) + pixels.contentHashCode()

    // printing every pixel would make it unreadable
    override fun toString(): String = "UvImage(width=$width, height=$height)"

    companion {
        /** Loads an image file: PNG, JPEG, or another format scrimage reads. */
        fun load(path: Path): UvImage {
            val image = ImmutableImage.loader().fromPath(path).awt()
            val argb = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
            // drop the alpha byte: textures are opaque
            return UvImage(image.width, image.height, IntArray(argb.size) { argb[it] and 0xFFFFFF })
        }

        private fun channel(value: Int): Double = (value and 0xFF) / 255.0
    }
}
