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

class Canvas(val width: Int, val height: Int) {
    private val pixels: Array<Color> = Array(width * height) { Color.BLACK }
    val size get() = pixels.size

    private fun coordinatesToIndex(x: Int, y: Int) = (y * width) + x

    private fun indexToCoordinates(index: Int) = Pair(index % width, index / width)

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
        pixels.asSequence().map(::clipColor).withIndex().forEach { (index, color) -> image.setColor(index, color) }
        image.forWriter(PngWriter().withMaxCompression()).write(path)
    }

    companion object {
        private const val MAX_COLOR_VALUE = 255

        @JvmStatic
        private fun clipValueToRange(color: Double): Int {
            return minOf((color * MAX_COLOR_VALUE).toInt(), MAX_COLOR_VALUE)
        }

        @JvmStatic
        private fun clipColor(color: Color): RGBColor {
            return RGBColor(
                clipValueToRange(color.red),
                clipValueToRange(color.green),
                clipValueToRange(color.blue),
            )
        }
    }
}
