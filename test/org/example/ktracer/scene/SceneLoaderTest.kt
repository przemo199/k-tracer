package org.example.ktracer.scene

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.PngWriter
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.colorproviders.mappings.CylindricalMapping
import org.example.ktracer.colorproviders.mappings.PlanarMapping
import org.example.ktracer.colorproviders.mappings.SphericalMapping
import org.example.ktracer.colorproviders.patterns.StripePattern
import org.example.ktracer.colorproviders.textures.CubeMap
import org.example.ktracer.colorproviders.textures.TextureMap
import org.example.ktracer.colorproviders.textures.UvAlignCheck
import org.example.ktracer.colorproviders.textures.UvCheckers
import org.example.ktracer.colorproviders.textures.UvImage
import org.example.ktracer.composites.Material
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.shapes.Cone
import org.example.ktracer.shapes.Cylinder
import org.example.ktracer.shapes.Shape
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SceneLoaderTest {
    private val camera = """
        - add: camera
          width: 10
          height: 10
          field-of-view: 1
          from: [0, 0, -5]
          to: [0, 0, 0]
          up: [0, 1, 0]
    """.trimIndent()

    /** Parses the camera above followed by [entries]; the first line of [entries] is line 8. */
    private fun parse(entries: String) = SceneLoader.parse(camera + "\n" + entries.trimIndent())

    private fun error(entries: String) = assertFailsWith<SceneException> { parse(entries) }.message!!

    private fun onlyShape(entries: String): Shape = parse(entries).world.shapes.single()

    @Test
    fun `every example scene loads`() {
        val scenes = Path("scenes").listDirectoryEntries("*.yaml")
        assertTrue(scenes.size >= 8)
        for (path in scenes) {
            val (world, camera) = SceneLoader.load(path)
            assertTrue(world.shapes.isNotEmpty(), "$path")
            assertTrue(world.lights.isNotEmpty(), "$path")
            assertTrue(camera.horizontalSize > 0, "$path")
        }
    }

    @Test
    fun `camera and light are read`() {
        val (world, camera) = parse("- add: light\n  at: [1, .5, 2.]\n  intensity: [1, 1.0, 0]")
        assertEquals(10, camera.horizontalSize)
        assertEquals(1.0, camera.fieldOfView)
        assertEquals(Point(1.0, 0.5, 2.0), world.lights.single().position)
        assertEquals(Color(1.0, 1.0, 0.0), world.lights.single().intensity)
    }

    @Test
    fun `shape without material gets the default material`() {
        assertEquals(Material(), onlyShape("- add: sphere").material)
    }

    @Test
    fun `material values are read`() {
        val shape = onlyShape(
            """
            - add: sphere
              material:
                color: [0.1, 0.2, 0.3]
                ambient: 0.4
                diffuse: 0.5
                specular: 0.6
                shininess: 7
                reflective: 0.8
                transparency: 0.9
                refractive-index: 1.5
                casts-shadow: false
            """,
        )
        val expected = Material(
            color = Color(0.1, 0.2, 0.3),
            ambient = 0.4,
            diffuse = 0.5,
            specular = 0.6,
            shininess = 7.0,
            reflectiveness = 0.8,
            transparency = 0.9,
            refractiveIndex = 1.5,
            castsShadow = false,
        )
        assertEquals(expected, shape.material)
    }

    @Test
    fun `materials extend their base over several levels and use named colors`() {
        val shape = onlyShape(
            """
            - define: red-color
              value: [1, 0, 0]
            - define: base-material
              value: { color: red-color, ambient: 0.3, diffuse: 0.4, specular: 0.5 }
            - define: middle-material
              extend: base-material
              value: { diffuse: 0.6 }
            - define: top-material
              extend: middle-material
              value: { specular: 0.7 }
            - add: sphere
              material: top-material
            """,
        )
        val material = shape.material
        assertEquals(Color(1.0, 0.0, 0.0), material.color)
        assertEquals(listOf(0.3, 0.6, 0.7), listOf(material.ambient, material.diffuse, material.specular))
    }

    @Test
    fun `definitions may come after the entries that use them`() {
        val shape = onlyShape(
            """
            - add: sphere
              material: late-material
            - define: late-material
              value: { ambient: 0.5 }
            """,
        )
        assertEquals(0.5, shape.material.ambient)
    }

    @Test
    fun `shape shadow flag overrides the material`() {
        assertFalse(onlyShape("- add: cube\n  shadow: false").material.castsShadow)
    }

    @Test
    fun `transform steps apply in list order wherever names appear`() {
        val shape = onlyShape(
            """
            - define: move-transform
              value: [[translate, 1, 0, 0]]
            - add: sphere
              transform: [[scale, 2, 2, 2], move-transform, [rotate-y, 0.5]]
            """,
        )
        val expected = Transformations.rotationY(0.5) *
            Transformations.translation(1.0, 0.0, 0.0) *
            Transformations.scaling(2.0, 2.0, 2.0)
        assertEquals(expected, shape.transformation)
    }

    @Test
    fun `defined transforms may build on other defined transforms`() {
        val shape = onlyShape(
            """
            - define: standard-transform
              value: [[translate, 1, -1, 1], [scale, 0.5, 0.5, 0.5]]
            - define: large-object
              value: [standard-transform, [scale, 3, 3, 3]]
            - add: cube
              transform: [large-object]
            """,
        )
        val expected = Transformations.scaling(3.0, 3.0, 3.0) *
            Transformations.scaling(0.5, 0.5, 0.5) *
            Transformations.translation(1.0, -1.0, 1.0)
        assertEquals(expected, shape.transformation)
    }

    @Test
    fun `pattern colors and transform are applied`() {
        val shape = onlyShape(
            """
            - define: blue-color
              value: [0, 0, 1]
            - add: sphere
              material:
                pattern:
                  type: stripes
                  colors: [[1, 0, 0], blue-color]
                  transform: [[scale, 0.5, 1, 1]]
            """,
        )
        val pattern = assertIs<StripePattern>(shape.material.colorProvider)
        // x = 0.75 lands in the second stripe once the stripes are scaled to half width
        assertEquals(Color(0.0, 0.0, 1.0), pattern.colorAtShape(shape, Point(0.75, 0.0, 0.0)))
        assertEquals(Color(1.0, 0.0, 0.0), pattern.colorAtShape(shape, Point(0.25, 0.0, 0.0)))
    }

    @Test
    fun `cylinders and cones read their limits`() {
        val [world, _] = parse(
            """
            - add: cylinder
              min: 0
              max: 0.5
              closed: true
            - add: cone
              min: -1
            """,
        )
        val cylinder = assertIs<Cylinder>(world.shapes[0])
        assertEquals(listOf(0.0, 0.5), listOf(cylinder.min, cylinder.max))
        assertTrue(cylinder.closed)
        val cone = assertIs<Cone>(world.shapes[1])
        assertEquals(-1.0, cone.min)
        assertFalse(cone.closed)
    }

    @Test
    fun `misspelled keys are reported with their location`() {
        assertContains(error("- add: sphere\n  materail: {}"), "scene:9:3: Unknown property 'materail'")
        assertContains(error("- add: sphere\n  material: { colr: [1, 0, 0] }"), "Unknown property 'colr'")
    }

    @Test
    fun `undefined names are reported with their location`() {
        assertEquals("scene:9:13: undefined material `glass-material`", error("- add: sphere\n  material: glass-material"))
        assertEquals("scene:9:15: undefined transform `big-object`", error("- add: sphere\n  transform: [big-object]"))
        assertContains(error("- add: sphere\n  material: { color: pink-color }"), "undefined color `pink-color`")
        assertContains(
            error("- define: a-material\n  extend: nope-material\n  value: { ambient: 0.5 }"),
            "undefined material `nope-material`",
        )
    }

    @Test
    fun `malformed values are rejected`() {
        assertContains(error("- add: sphere\n  transform: [[rotate-y, 1, 2]]"), "Expected [rotate-y, radians]")
        assertContains(error("- add: sphere\n  transform: [[scale, 2]]"), "Expected [scale, x, y, z]")
        assertContains(error("- add: sphere\n  transform: [[shrink, 2]]"), "Unknown transform operation `shrink`")
        assertContains(error("- add: sphere\n  transform: [[scale, a, 1, 1]]"), "scene:9:23: Value for 'transform' is invalid")
        assertContains(error("- add: light\n  at: [1, 2]\n  intensity: [1, 1, 1]"), "Expected [x, y, z]")
        assertContains(error("- add: sphere\n  material: { pattern: { type: stripes, colors: [[1, 1, 1]] } }"), "two colors")
        assertContains(error("- add: sphere\n  material: { pattern: { type: zigzag, colors: [] } }"), "zigzag")
        assertContains(error("- define: odd\n  value: [1, scale]"), "Expected a material")
    }

    @Test
    fun `unknown entries and misplaced options are rejected`() {
        assertContains(error("- add: torus"), "scene:8:3: Unknown `add: torus`")
        assertContains(error("- remove: sphere"), "needs an `add` or a `define`")
        assertContains(error("- add: sphere\n  closed: true"), "scene:8:3: `min`, `max` and `closed` only apply")
        assertContains(
            error("- define: big-transform\n  extend: other\n  value: [[scale, 2, 2, 2]]"),
            "`big-transform`: only materials can use `extend`",
        )
    }

    @Test
    fun `non-invertible transforms are rejected`() {
        assertContains(error("- add: sphere\n  transform: [[scale, 0, 1, 1]]"), "not invertible")
        assertContains(error("- define: flat\n  value: [[scale, 1, 0, 1]]"), "not invertible")
    }

    @Test
    fun `scene needs exactly one valid camera`() {
        assertFailsWith<SceneException> { SceneLoader.parse("- add: sphere") }.let {
            assertEquals("scene: the scene has no camera", it.message)
        }
        assertContains(error(camera), "more than one camera")
        val stuck = assertFailsWith<SceneException> { SceneLoader.parse(camera.replace("to: [0, 0, 0]", "to: [0, 0, -5]")) }
        assertContains(stuck.message!!, "scene:1:3: camera: `from` must differ from `to`")
        val flat = assertFailsWith<SceneException> { SceneLoader.parse(camera.replace("height: 10", "height: 0")) }
        assertContains(flat.message!!, "`width` and `height` must be positive")
    }

    @Test
    fun `scene must be a list of entries`() {
        assertContains(assertFailsWith<SceneException> { SceneLoader.parse("add: sphere") }.message!!, "is a list")
        assertContains(assertFailsWith<SceneException> { SceneLoader.parse("- [unclosed") }.message!!, "scene:")
    }

    @Test
    fun `inline materials can extend a defined one`() {
        val shape = onlyShape(
            """
            - define: glossy-material
              value: { specular: 0.4, shininess: 100 }
            - add: sphere
              material:
                extend: glossy-material
                reflective: 0.1
            """,
        )
        assertEquals(Material(specular = 0.4, shininess = 100.0, reflectiveness = 0.1), shape.material)
        assertContains(error("- add: sphere\n  material: { extend: nope-material }"), "scene:8:3: undefined material")
    }

    private fun onlyColorProvider(entries: String): ColorProvider = onlyShape(entries).material.colorProvider!!

    private val checkers = """
        uv_pattern:
          type: checkers
          width: 16
          height: 8
          colors: [[0, 0.5, 0], [1, 1, 1]]
    """.trimIndent()

    @ParameterizedTest
    @CsvSource("planar", "spherical", "cylindrical")
    fun `texture maps load with each mapping`(name: String) {
        val provider = onlyColorProvider(
            "- add: sphere\n  material:\n    pattern:\n      type: map\n      mapping: $name\n" + checkers.prependIndent("      "),
        )
        val mapping = mapOf("planar" to PlanarMapping, "spherical" to SphericalMapping, "cylindrical" to CylindricalMapping)
        val expected = TextureMap(UvCheckers(16, 8, Color(0, 0.5, 0), Color.WHITE), mapping.getValue(name))
        assertEquals<ColorProvider>(expected, provider)
    }

    @Test
    fun `align check loads with named colors and transform`() {
        val provider = onlyColorProvider(
            """
            - define: red-color
              value: [1, 0, 0]
            - add: cylinder
              material:
                pattern:
                  type: map
                  mapping: cylindrical
                  transform: [[rotate-y, 1]]
                  uv_pattern:
                    type: align_check
                    colors:
                      main: [1, 1, 1]
                      ul: red-color
                      ur: [1, 1, 0]
                      bl: [0, 1, 0]
                      br: [0, 1, 1]
            """,
        )
        val alignCheck = UvAlignCheck(Color.WHITE, Color.RED, Color(1, 1, 0), Color.GREEN, Color(0, 1, 1))
        assertEquals<ColorProvider>(TextureMap(alignCheck, CylindricalMapping, Transformations.rotationY(1.0)), provider)
    }

    /** A sphere whose material has a texture map with [body] as its keys after `type: map`. */
    private fun mapError(body: String, shape: String = "sphere") =
        error("- add: $shape\n  material:\n    pattern:\n      type: map\n" + body.trimIndent().prependIndent("      "))

    @Test
    fun `invalid texture maps are rejected`() {
        assertContains(mapError("mapping: cubic\n$checkers"), "cubic")
        assertContains(mapError("mapping: planar\nuv_pattern: { type: noise }"), "noise")
        assertContains(mapError("mapping: planar\n" + checkers.replace("width", "widht")), "widht")
        assertContains(mapError("mapping: planar\ncolors: [[0, 0, 0], [1, 1, 1]]\n$checkers"), "colors")
        assertContains(mapError("mapping: planar"), "scene:8:3: a texture map needs a `uv_pattern`")
        assertContains(
            mapError("mapping: planar\nuv_pattern: { type: checkers, width: 0, height: 2, colors: [[0, 0, 0], [1, 1, 1]] }"),
            "positive `width` and `height`",
        )
    }

    private fun face(name: String, main: String) =
        "$name: { type: align_check, colors: { main: $main, ul: [1, 0, 0], ur: [1, 1, 0], bl: [0, 1, 0], br: [0, 1, 1] } }"

    private val faceNames = listOf("left", "front", "right", "back", "up", "down")

    @Test
    fun `cube maps load one pattern per face`() {
        val faces = faceNames.mapIndexed { index, name -> face(name, "[0.$index, 0, 0]") }
        val provider = onlyColorProvider(
            "- add: cube\n  material:\n    pattern:\n      type: map\n      mapping: cube\n" +
                faces.joinToString("\n").prependIndent("      "),
        )
        fun face(index: Int) = UvAlignCheck(Color(index / 10.0, 0.0, 0.0), Color.RED, Color(1, 1, 0), Color.GREEN, Color(0, 1, 1))
        assertEquals<ColorProvider>(CubeMap(face(0), face(1), face(2), face(3), face(4), face(5)), provider)
    }

    @Test
    fun `invalid cube maps are rejected`() {
        val fiveFaces = faceNames.dropLast(1).joinToString("\n") { face(it, "[1, 1, 1]") }
        assertContains(mapError("mapping: cube\n$fiveFaces", "cube"), "cube map: missing the `down` face")
        assertContains(
            mapError("mapping: cube\n" + face("uv_pattern", "[1, 1, 1]"), "cube"),
            "instead of `uv_pattern`",
        )
        assertContains(mapError("mapping: spherical\n" + face("front", "[1, 1, 1]")), "only apply to `mapping: cube`")
    }

    @Test
    fun `texture images are resolved relative to the scene file`(@TempDir directory: Path) {
        val image = BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB).apply {
            setRGB(0, 0, 0xFF0000)
            setRGB(1, 0, 0x0000FF)
        }
        directory.resolve("textures").createDirectories()
        ImmutableImage.wrapAwt(image).output(PngWriter.NoCompression, directory.resolve("textures/stripe.png"))
        val scene = directory.resolve("scene.yaml")
        val sphere = "- add: sphere\n  material:\n    pattern:\n      type: map\n      mapping: spherical\n" +
            "      uv_pattern: { type: image, file: %s }"
        scene.writeText(camera + "\n" + sphere.format("textures/stripe.png"))
        val provider = SceneLoader.load(scene).world.shapes.single().material.colorProvider
        assertEquals<ColorProvider?>(TextureMap(UvImage(2, 1, intArrayOf(0xFF0000, 0x0000FF)), SphericalMapping), provider)

        scene.writeText(camera + "\n" + sphere.format("missing.png"))
        val message = assertFailsWith<SceneException> { SceneLoader.load(scene) }.message!!
        assertContains(message, "$scene:13:40: cannot load texture ${directory.resolve("missing.png")}: no such file")
    }
}
