package org.example.ktracer.composites

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import com.sksamuel.scrimage.nio.PngWriter
import java.awt.image.BufferedImage
import java.io.IOException
import kotlin.math.roundToInt
import org.example.ktracer.primitives.Color

class Canvas(val width: Int, val height: Int) : Iterable<Color> {
    val size = width * height
    private val _pixels: Array<Color> = Array(size) { DEFAULT_COLOR }
    val pixels: List<Color> = _pixels.asList()

    fun coordinatesToIndex(x: Int, y: Int) = (y * width) + x

    fun indexToCoordinates(index: Int) = Pair(index % width, index / width)

    operator fun get(index: Int): Color = _pixels[index]

    operator fun set(index: Int, color: Color) {
        _pixels[index] = color
    }

    operator fun get(x: Int, y: Int): Color = _pixels[coordinatesToIndex(x, y)]

    operator fun set(x: Int, y: Int, color: Color) {
        _pixels[coordinatesToIndex(x, y)] = color
    }

    override operator fun iterator(): Iterator<Color> = _pixels.iterator()

    @Throws(IOException::class)
    fun saveAsPng(path: String) {
        val image = ImmutableImage.create(width, height, BufferedImage.TYPE_INT_RGB).blank()
        _pixels
            .withIndex()
            .forEach { (index, color) -> image.setColor(index, color.toRGBColor()) }
        image.forWriter(PngWriter().withMaxCompression()).write(path)
    }

    private fun Color.toRGBColor(): RGBColor {
        val (red, green, blue) = clamped().map(MAX_COLOR_VALUE::times)
        return RGBColor(red.roundToInt(), green.roundToInt(), blue.roundToInt())
    }

    companion {
        const val MIN_COLOR_VALUE = 0.0

        const val MAX_COLOR_VALUE = 255.0

        val DEFAULT_COLOR = World.DEFAULT_COLOR
    }
}
