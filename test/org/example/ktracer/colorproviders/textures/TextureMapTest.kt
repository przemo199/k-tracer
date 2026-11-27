package org.example.ktracer.colorproviders.textures

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.example.ktracer.colorproviders.mappings.PlanarMapping
import org.example.ktracer.colorproviders.mappings.SphericalMapping
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.shapes.Cube
import org.example.ktracer.shapes.Plane
import org.example.ktracer.shapes.Sphere
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TextureMapTest {
    private fun checkers(width: Int, height: Int) = UvCheckers(width, height, Color.BLACK, Color.WHITE)

    @ParameterizedTest
    @CsvSource(
        "0.4315, 0.4670, 0.7719, false",
        "-0.9654, 0.2552, -0.0534, true",
        "0.1039, 0.7090, 0.6975, false",
        "-0.4986, -0.7856, -0.3663, true",
        "-0.0317, -0.9395, 0.3411, true",
        "0.4809, -0.7721, 0.4154, true",
        "0.0285, -0.9612, -0.2745, true",
        "-0.5734, -0.2162, -0.7903, false",
        "0.7688, -0.1470, 0.6223, true",
        "-0.7652, 0.2175, 0.6060, true",
    )
    fun `checkers on a sphere with spherical mapping`(x: Double, y: Double, z: Double, isBlack: Boolean) {
        val texture = TextureMap(checkers(16, 8), SphericalMapping)
        assertEquals(if (isBlack) Color.BLACK else Color.WHITE, texture.colorAtShape(Sphere(), Point(x, y, z)))
    }

    @Test
    fun `shape transformation applies`() {
        val texture = TextureMap(checkers(2, 2), PlanarMapping)
        val plane = Plane(transformation = Transformations.translation(0.5, 0, 0))
        // world x = 0.75 is object x = 0.25: the first square, not the second
        assertEquals(Color.BLACK, texture.colorAtShape(plane, Point(0.75, 0, 0)))
    }

    @Test
    fun `texture transformation applies`() {
        val texture = TextureMap(checkers(2, 2), PlanarMapping)
        val point = Point(0.75, 0, 0)
        assertEquals(Color.WHITE, texture.colorAtShape(Plane(), point))
        // twice as large squares: x = 0.75 becomes u = 0.375, the first square
        val scaled = TextureMap(texture.uvPattern, texture.mapping, Transformations.scaling(2, 2, 2))
        assertEquals(Color.BLACK, scaled.colorAtShape(Plane(), point))
    }

    @Test
    fun `texture maps compare by value`() {
        assertEquals(TextureMap(checkers(4, 2), SphericalMapping), TextureMap(checkers(4, 2), SphericalMapping))
        assertNotEquals(TextureMap(checkers(4, 2), SphericalMapping), TextureMap(checkers(4, 2), PlanarMapping))
    }
}

class CubeMapTest {
    private val yellow = Color(1, 1, 0)

    /** Each face: its own main color, the same four corner colors. */
    private fun alignChecks(): CubeMap {
        fun face(red: Double) = UvAlignCheck(Color(red, 0.0, 0.0), Color.RED, yellow, Color.GREEN, Color.WHITE)
        return CubeMap(face(0.1), face(0.2), face(0.3), face(0.4), face(0.5), face(0.6))
    }

    @ParameterizedTest
    @CsvSource(
        "-1, 0, 0, 0.1, 0, 0",
        "0, 0, 1, 0.2, 0, 0",
        "1, 0, 0, 0.3, 0, 0",
        "0, 0, -1, 0.4, 0, 0",
        "0, 1, 0, 0.5, 0, 0",
        "0, -1, 0, 0.6, 0, 0",
        // front face: u grows with x, v with y
        "-0.9, 0.9, 1, 1, 0, 0",
        "0.9, 0.9, 1, 1, 1, 0",
        "-0.9, -0.9, 1, 0, 1, 0",
        "0.9, -0.9, 1, 1, 1, 1",
        // back face: u grows towards -x
        "0.9, 0.9, -1, 1, 0, 0",
        "-0.9, -0.9, -1, 1, 1, 1",
        // top face: v grows towards -z
        "-0.9, 1, -0.9, 1, 0, 0",
        // bottom face: v grows towards +z
        "-0.9, -1, 0.9, 1, 0, 0",
    )
    fun `each face shows its own pattern`(x: Double, y: Double, z: Double, red: Double, green: Double, blue: Double) {
        assertEquals(Color(red, green, blue), alignChecks().colorAtShape(Cube(), Point(x, y, z)))
    }

    @Test
    fun `cube map transformation applies`() {
        // a quarter turn about y brings the right face (+x) to the front (+z)
        val cubeMap = with(alignChecks()) { CubeMap(left, front, right, back, up, down, Transformations.rotationY(-PI / 2)) }
        assertEquals(Color(0.3, 0, 0), cubeMap.colorAtShape(Cube(), Point(0, 0, 1)))
    }

    @Test
    fun `cube maps compare face by face`() {
        assertEquals(alignChecks(), alignChecks())
        val other = with(alignChecks()) { CubeMap(left, front, right, back, UvAlignCheck(yellow, yellow, yellow, yellow, yellow), down) }
        assertNotEquals(alignChecks(), other)
    }
}
