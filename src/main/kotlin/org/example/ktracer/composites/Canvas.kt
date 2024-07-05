package org.example.ktracer.composites

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import com.sksamuel.scrimage.nio.PngWriter
import org.example.ktracer.primitives.Color
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.jvm.Throws
import kotlin.math.roundToInt

class Canvas(val width: Int, val height: Int) {
    private val pixels: Array<Color> = Array(width * height) { DEFAULT_COLOR }
    val size get() = pixels.size

    fun coordinatesToIndex(x: Int, y: Int) = (y * width) + x

    fun indexToCoordinates(index: Int) = Pair(index % width, index / width)

    fun coordinateStream(): Stream<Pair<Int, Int>> {
        return IntStream.range(0, size).mapToObj(::indexToCoordinates)
    }

    operator fun get(index: Int): Color {
        return pixels[index]
    }

    operator fun set(index: Int, color: Color) {
        pixels[index] = color
    }

    operator fun get(x: Int, y: Int): Color {
        return pixels[coordinatesToIndex(x, y)]
    }

    operator fun set(x: Int, y: Int, color: Color) {
        pixels[coordinatesToIndex(x, y)] = color
    }

    operator fun iterator(): Iterator<Color> {
        return pixels.iterator()
    }

    @Throws(IOException::class)
    fun saveAsPng(path: String) {
        val image = ImmutableImage.create(width, height, BufferedImage.TYPE_INT_RGB).blank()
        pixels.asSequence()
            .map(Color::clamp)
            .map { it.toRGBColor() }
            .withIndex()
            .forEach { (index, color) -> image.setColor(index, color) }
        image.forWriter(PngWriter().withMaxCompression()).write(path)
    }

    companion object {
        const val MIN_COLOR_VALUE = 0.0

        const val MAX_COLOR_VALUE = 255.0

        @JvmField
        val DEFAULT_COLOR = World.DEFAULT_COLOR

        fun Color.toRGBColor(): RGBColor {
            val (red, green, blue) = map { it * MAX_COLOR_VALUE }
            return RGBColor(red.roundToInt(), green.roundToInt(), blue.roundToInt())
        }
    }
}
