package org.example.ktracer.scene

import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.example.ktracer.colorproviders.patterns.StripePattern
import org.example.ktracer.composites.Material
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.shapes.Cone
import org.example.ktracer.shapes.Cylinder
import org.example.ktracer.shapes.Shape

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
}
