package org.example.ktracer.colorproviders.textures

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

private val YELLOW = Color(1, 1, 0)
private val CYAN = Color(0, 1, 1)

class UvCheckersTest {
    private val checkers = UvCheckers(2, 2, Color.BLACK, Color.WHITE)

    @ParameterizedTest
    @CsvSource("0.0, 0.0, true", "0.5, 0.0, false", "0.0, 0.5, false", "0.5, 0.5, true", "1.0, 1.0, true")
    fun `checker pattern in 2D`(u: Double, v: Double, isBlack: Boolean) {
        assertEquals(if (isBlack) Color.BLACK else Color.WHITE, checkers.colorAt(Coordinates(u, v)))
    }

    @Test
    fun `checker pattern uses separate width and height`() {
        val checkers = UvCheckers(4, 2, Color.BLACK, Color.WHITE)
        assertEquals(Color.BLACK, checkers.colorAt(Coordinates(0.0, 0.4)))
        assertEquals(Color.WHITE, checkers.colorAt(Coordinates(0.0, 0.6)))
        assertEquals(Color.WHITE, checkers.colorAt(Coordinates(0.3, 0.0)))
    }

    @Test
    fun `checkers stay regular below zero`() {
        // rounding toward zero would put -0.25 in the same square as 0.25
        assertEquals(Color.BLACK, checkers.colorAt(Coordinates(0.25, 0.0)))
        assertEquals(Color.WHITE, checkers.colorAt(Coordinates(-0.25, 0.0)))
        assertEquals(Color.BLACK, checkers.colorAt(Coordinates(-0.75, 0.0)))
    }

    @Test
    fun `checkers need a positive size`() {
        assertFailsWith<IllegalArgumentException> { UvCheckers(0, 2, Color.BLACK, Color.WHITE) }
    }
}

class UvAlignCheckTest {
    @ParameterizedTest
    @CsvSource(
        "0.5, 0.5, main",
        "0.1, 0.9, upperLeft",
        "0.9, 0.9, upperRight",
        "0.1, 0.1, bottomLeft",
        "0.9, 0.1, bottomRight",
        "0.5, 0.9, main",
        "0.1, 0.5, main",
    )
    fun `layout of the align check pattern`(u: Double, v: Double, region: String) {
        val colors = mapOf(
            "main" to Color.WHITE,
            "upperLeft" to Color.RED,
            "upperRight" to YELLOW,
            "bottomLeft" to Color.GREEN,
            "bottomRight" to CYAN,
        )
        val pattern = UvAlignCheck(Color.WHITE, Color.RED, YELLOW, Color.GREEN, CYAN)
        assertEquals(colors.getValue(region), pattern.colorAt(Coordinates(u, v)))
    }
}

class UvImageTest {
    /** 2×2 image: red and green on the top row, blue and white on the bottom row. */
    private val corners = UvImage(2, 2, intArrayOf(0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF))

    @ParameterizedTest
    @CsvSource(
        "0.0, 1.0, 1, 0, 0",
        "1.0, 1.0, 0, 1, 0",
        "0.0, 0.0, 0, 0, 1",
        "1.0, 0.0, 1, 1, 1",
        "0.4, 0.6, 1, 0, 0",
        "0.6, 0.4, 1, 1, 1",
    )
    fun `v runs from the bottom row to the top`(u: Double, v: Double, red: Double, green: Double, blue: Double) {
        assertEquals(Color(red, green, blue), corners.colorAt(Coordinates(u, v)))
    }

    @Test
    fun `channels are scaled to 0-1`() {
        val image = UvImage(1, 1, intArrayOf(0xFF3300))
        assertEquals(Color(1, 0.2, 0), image.colorAt(Coordinates(0.5, 0.5)))
    }

    @Test
    fun `coordinates outside 0-1 are clamped`() {
        assertEquals(Color.GREEN, corners.colorAt(Coordinates(1.5, 2.0)))
        assertEquals(Color.BLUE, corners.colorAt(Coordinates(-0.5, -1.0)))
        assertEquals(Color.RED, corners.colorAt(Coordinates(Double.NaN, Double.NaN)))
    }

    @Test
    fun `loads an image file`(@TempDir directory: Path) {
        val path = directory.resolve("corners.png")
        val image = BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)
        listOf(0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF).forEachIndexed { index, rgb -> image.setRGB(index % 2, index / 2, rgb) }
        ImmutableImage.wrapAwt(image).output(PngWriter.NoCompression, path)
        assertEquals(corners, UvImage.load(path))
    }

    @Test
    fun `missing file is an error`() {
        assertFails { UvImage.load(Path("does-not-exist.png")) }
    }
}
