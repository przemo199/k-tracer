package org.example.ktracer.scene

import com.charleskorn.kaml.Location
import kotlin.math.PI
import org.example.ktracer.colorproviders.patterns.CheckerPattern
import org.example.ktracer.colorproviders.patterns.GradientPattern
import org.example.ktracer.colorproviders.patterns.Pattern
import org.example.ktracer.colorproviders.patterns.RingPattern
import org.example.ktracer.colorproviders.patterns.StripePattern
import org.example.ktracer.composites.Camera
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.World
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Cone
import org.example.ktracer.shapes.Cube
import org.example.ktracer.shapes.Cylinder
import org.example.ktracer.shapes.Plane
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Sphere

/** A scene entry and where it starts in the file. */
data class Located<out T>(val value: T, val location: Location)

/** Turns parsed scene entries into a [World] and a [Camera]. [source] names the file in error messages. */
class SceneResolver(private val source: String) {
    private val colors = HashMap<String, Color>()

    /** Stored with `extend` already applied. */
    private val materials = HashMap<String, MaterialDef>()
    private val transforms = HashMap<String, Transformation>()

    fun build(entries: List<Located<Entry>>): Scene {
        // definitions first, so that `add` entries may use names defined further down the file
        val additions = ArrayList<Located<AddEntry>>()
        for ([entry, location] in entries) {
            when (entry) {
                is DefineEntry -> define(entry, location)
                is AddEntry -> additions += Located(entry, location)
            }
        }

        val world = World(mutableListOf(), mutableListOf())
        var camera: Camera? = null
        for ([entry, location] in additions) {
            when (entry) {
                is CameraEntry -> {
                    if (camera != null) fail(location, "the scene has more than one camera")
                    camera = camera(entry, location)
                }
                is LightEntry -> world.lights += Light(entry.at.toPoint(), entry.intensity.toColor())
                is ShapeEntry -> world.shapes += shape(entry, location)
            }
        }
        return Scene(world, camera ?: throw SceneException("$source: the scene has no camera"))
    }

    private fun define(entry: DefineEntry, location: Location) {
        val name = entry.define
        val value = entry.value
        if (entry.extend != null && value !is DefineValue.Material) {
            fail(location, "`$name`: only materials can use `extend`")
        }
        when (value) {
            is DefineValue.Material -> {
                val base = entry.extend?.let { materials[it] ?: fail(location, "undefined material `$it`") }
                materials[name] = base?.let(value.material::over) ?: value.material
            }
            is DefineValue.Transform -> transforms[name] = transformation(value.steps)
            is DefineValue.Color -> colors[name] = value.color.toColor()
        }
    }

    private fun camera(entry: CameraEntry, location: Location): Camera {
        if (entry.width <= 0 || entry.height <= 0) {
            fail(location, "camera: `width` and `height` must be positive")
        }
        if (entry.fieldOfView <= 0.0 || entry.fieldOfView >= PI) {
            fail(location, "camera: `field-of-view` must be between 0 and π radians")
        }
        val view = Transformations.viewTransform(entry.from.toPoint(), entry.to.toPoint(), entry.up.toVector())
        if (!view.isInvertible()) {
            fail(location, "camera: `from` must differ from `to`, and `up` must not point along the view direction")
        }
        return Camera(entry.width, entry.height, entry.fieldOfView).apply { transformation = view }
    }

    private fun shape(entry: ShapeEntry, location: Location): Shape {
        var material = entry.material?.let(::material) ?: Material()
        entry.shadow?.let { material = material.copy(castsShadow = it) }
        val transformation = transformation(entry.transform)
        val isCapped = entry.add == ShapeKind.CYLINDER || entry.add == ShapeKind.CONE
        if (!isCapped && (entry.min != null || entry.max != null || entry.closed != null)) {
            fail(location, "`min`, `max` and `closed` only apply to cylinders and cones")
        }
        return when (entry.add) {
            ShapeKind.PLANE -> Plane(material, transformation)
            ShapeKind.SPHERE -> Sphere(material, transformation)
            ShapeKind.CUBE -> Cube(material, transformation)
            ShapeKind.CYLINDER -> Cylinder(material = material, transformation = transformation).apply {
                entry.min?.let { min = it }
                entry.max?.let { max = it }
                entry.closed?.let { closed = it }
            }
            ShapeKind.CONE -> Cone(material = material, transformation = transformation).apply {
                entry.min?.let { min = it }
                entry.max?.let { max = it }
                entry.closed?.let { closed = it }
            }
        }
    }

    private fun material(reference: Ref<MaterialDef>): Material {
        val definition = resolve(reference, materials, "material") { it }
        val default = Material()
        return Material(
            color = definition.color?.let(::color) ?: default.color,
            ambient = definition.ambient ?: default.ambient,
            diffuse = definition.diffuse ?: default.diffuse,
            specular = definition.specular ?: default.specular,
            shininess = definition.shininess ?: default.shininess,
            reflectiveness = definition.reflective ?: default.reflectiveness,
            refractiveIndex = definition.refractiveIndex ?: default.refractiveIndex,
            transparency = definition.transparency ?: default.transparency,
            castsShadow = definition.castsShadow ?: default.castsShadow,
            colorProvider = definition.pattern?.let(::pattern),
        )
    }

    private fun pattern(definition: PatternDef): Pattern {
        val first = color(definition.colors.first)
        val second = color(definition.colors.second)
        val transformation = transformation(definition.transform)
        return when (definition.type) {
            PatternType.STRIPES -> StripePattern(first, second, transformation)
            PatternType.GRADIENT -> GradientPattern(first, second, transformation)
            PatternType.RINGS -> RingPattern(first, second, transformation)
            PatternType.CHECKERS -> CheckerPattern(first, second, transformation)
        }
    }

    private fun color(reference: Ref<Triplet>): Color = resolve(reference, colors, "color") { it.toColor() }

    /** Steps apply in list order: `[a, b]` means "a, then b", that is the matrix `b * a`. */
    private fun transformation(steps: List<Step>): Transformation =
        steps.fold(Transformation.identity()) { transformation, step ->
            resolve(step, transforms, "transform") { it.matrix } * transformation
        }

    private fun <T, R> resolve(reference: Ref<T>, definitions: Map<String, R>, kind: String, inline: (T) -> R): R =
        when (reference) {
            is Ref.Named -> definitions[reference.name] ?: fail(reference.location, "undefined $kind `${reference.name}`")
            is Ref.Inline -> inline(reference.value)
        }

    private fun fail(location: Location, message: String): Nothing =
        throw SceneException("$source:${location.line}:${location.column}: $message")
}

private fun Triplet.toPoint() = Point(x, y, z)

private fun Triplet.toVector() = Vector(x, y, z)

private fun Triplet.toColor() = Color(x, y, z)
