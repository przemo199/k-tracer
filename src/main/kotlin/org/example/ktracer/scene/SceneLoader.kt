package org.example.ktracer.scene

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.example.ktracer.composites.Camera
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.World
import org.example.ktracer.patterns.CheckerPattern
import org.example.ktracer.patterns.GradientPattern
import org.example.ktracer.patterns.Pattern
import org.example.ktracer.patterns.RingPattern
import org.example.ktracer.patterns.StripePattern
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Matrix
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import org.example.ktracer.scene.Keyword.DEFINE
import org.example.ktracer.scene.Keyword.EXTEND
import org.example.ktracer.shapes.Cone
import org.example.ktracer.shapes.Cube
import org.example.ktracer.shapes.Cylinder
import org.example.ktracer.shapes.Plane
import org.example.ktracer.shapes.Sphere
import java.io.File

class SceneLoader {
    val colorDefinitions = HashMap<String, Color>()
    val materialDefinitions = HashMap<String, Material>()
    val patternDefinitions = HashMap<String, Pattern>()
    val transformationDefinitions = HashMap<String, Matrix>()

    fun processDefinitions(yaml: JsonNode) {
        yaml.forEach {
            if (it.has(DEFINE)) {
                val name = it[DEFINE]!!.asText()
                when {
                    name.endsWith("-color") -> colorDefinitions[name] = parseColor(it)
                    name.endsWith("-material") -> materialDefinitions[name] = parseMaterial(it)
                    name.endsWith("-pattern") -> patternDefinitions[name] = parsePattern(it)
                    name.endsWith("-transform") || name.endsWith("-object") -> {
                        transformationDefinitions[name] = parseTransformation(it)
                    }
                }
            }
        }
    }

    fun parseArray(yaml: Iterable<JsonNode>): List<Double> {
        return yaml.map(JsonNode::doubleValue)
    }

    private fun parseColor(yaml: JsonNode): Color {
        if (yaml.isArray) {
            val colorChannels = parseArray(yaml)
            return Color(colorChannels[0], colorChannels[1], colorChannels[2])
        }
        return colorDefinitions[yaml.asText()]!!
    }

    private fun parsePattern(yaml: JsonNode): Pattern {
        val colors = yaml[Keyword.COLORS]
        val colorA = parseColor(colors[0])
        val colorB = parseColor(colors[1])
        val transformation = yaml[Keyword.TRANSFORM]?.let(::parseTransformation)
        val pattern = when (yaml[Keyword.TYPE].asText()) {
            "stripes" -> StripePattern(colorA, colorB)
            "gradient" -> GradientPattern(colorA, colorB)
            "rings" -> RingPattern(colorA, colorB)
            "checkers" -> CheckerPattern(colorA, colorB)
            else -> throw UnsupportedOperationException()
        }
        transformation?.let(pattern::transformation::set)
        return pattern
    }

    private fun parseMaterial(yaml: JsonNode): Material {
        if (yaml.isTextual) {
            return materialDefinitions[yaml.asText()]!!.copy()
        }

        val material =
            yaml[EXTEND]?.let {
                materialDefinitions[it.asText()]?.copy()
            } ?: Material()

        val materialYaml = yaml[Keyword.VALUE] ?: yaml

        materialYaml[Keyword.COLOR]?.let {
            material.color = parseColor(it)
        }

        materialYaml[Keyword.AMBIENT]?.let {
            material.ambient = it.doubleValue()
        }

        materialYaml[Keyword.DIFFUSE]?.let {
            material.diffuse = it.doubleValue()
        }

        materialYaml[Keyword.SPECULAR]?.let {
            material.specular = it.doubleValue()
        }

        materialYaml[Keyword.SHININESS]?.let {
            material.shininess = it.doubleValue()
        }

        materialYaml[Keyword.REFLECTIVE]?.let {
            material.reflectiveness = it.doubleValue()
        }

        materialYaml[Keyword.TRANSPARENCY]?.let {
            material.transparency = it.doubleValue()
        }

        materialYaml[Keyword.REFRACTIVE_INDEX]?.let {
            material.refractiveIndex = it.doubleValue()
        }

        materialYaml[Keyword.CASTS_SHADOW]?.let {
            material.castsShadow = it.booleanValue()
        }

        materialYaml[Keyword.PATTERN]?.let {
            material.pattern = parsePattern(it)
        }

        return material
    }

    private fun parseTransformation(yaml: JsonNode?): Transformation {
        var transformation = Transformation.IDENTITY
        val transformationYaml = yaml?.get(Keyword.VALUE) ?: yaml

        transformationYaml?.forEach {
            when {
                it.isTextual -> {
                    transformation = transformationDefinitions[it.asText()]!!.copy()
                }
                it.isArray -> {
                    val array = it as ArrayNode
                    when (val operation = array.remove(0).asText()) {
                        "scale" -> {
                            val values = parseArray(array)
                            transformation = Transformations.scaling(values[0], values[1], values[2]) * transformation
                        }
                        "translate" -> {
                            val values = parseArray(array)
                            transformation =
                                Transformations.translation(values[0], values[1], values[2]) * transformation
                        }
                        "rotate-x" -> {
                            val value = array[0].doubleValue()
                            transformation = Transformations.rotationX(value) * transformation
                        }
                        "rotate-y" -> {
                            val value = array[0].doubleValue()
                            transformation = Transformations.rotationY(value) * transformation
                        }
                        "rotate-z" -> {
                            val value = array[0].doubleValue()
                            transformation = Transformations.rotationZ(value) * transformation
                        }
                        else -> throw UnsupportedOperationException(operation)
                    }
                }
            }
        }

        return transformation
    }

    private fun parseMaterialAndTransform(yaml: JsonNode): Pair<Material, Transformation> {
        val material = parseMaterial(yaml[Keyword.MATERIAL])
        val transformation = parseTransformation(yaml[Keyword.TRANSFORM])
        return Pair(material, transformation)
    }

    private fun parseClosedAndMinAndMax(yaml: JsonNode): Triple<Boolean?, Double?, Double?> {
        val closed: Boolean? = yaml["closed"]?.booleanValue()
        val min = yaml["min"]?.doubleValue()
        val max = yaml["max"]?.doubleValue()
        return Triple(closed, min, max)
    }

    fun parseScene(yaml: JsonNode): Pair<World, Camera> {
        val world = World(mutableListOf(), mutableListOf())
        var camera = Camera(0, 0, 0)

        yaml.forEach {
            it[Keyword.ADD]?.asText().let { name ->
                when (name) {
                    "camera" -> {
                        val horizontalSize = it["width"].intValue()
                        val verticalSize = it["height"].intValue()
                        val fieldOfView = it["field-of-view"].doubleValue()
                        val fromValues = parseArray(it["from"])
                        val toValues = parseArray(it["to"])
                        val upValues = parseArray(it["up"])
                        val from = Point(fromValues[0], fromValues[1], fromValues[2])
                        val to = Point(toValues[0], toValues[1], toValues[2])
                        val up = Vector(upValues[0], upValues[1], upValues[2])
                        camera = Camera(horizontalSize, verticalSize, fieldOfView)
                        camera.transformation = Transformations.viewTransform(from, to, up)
                    }
                    "light" -> {
                        val positionValues = parseArray(it["at"])
                        val intensityValues = parseArray(it["intensity"])
                        val position = Point(positionValues[0], positionValues[1], positionValues[2])
                        val intensity = Color(intensityValues[0], intensityValues[1], intensityValues[2])
                        world.lights += Light(position, intensity)
                    }
                    "plane" -> {
                        val (material, transformation) = parseMaterialAndTransform(it)
                        world.shapes += Plane(material, transformation)
                    }
                    "sphere" -> {
                        val (material, transformation) = parseMaterialAndTransform(it)
                        world.shapes += Sphere(material, transformation)
                    }
                    "cube" -> {
                        val (material, transformation) = parseMaterialAndTransform(it)
                        world.shapes += Cube(material, transformation)
                    }
                    "cone" -> {
                        val (material, transformation) = parseMaterialAndTransform(it)
                        val (closed, min, max) = parseClosedAndMinAndMax(it)
                        val cone = Cone(material = material, transformation = transformation)
                        closed?.let(cone::closed::set)
                        min?.let(cone::min::set)
                        max?.let(cone::max::set)
                        world.shapes += cone
                    }
                    "cylinder" -> {
                        val (material, transformation) = parseMaterialAndTransform(it)
                        val (closed, min, max) = parseClosedAndMinAndMax(it)
                        val cylinder = Cylinder(material = material, transformation = transformation)
                        closed?.let(cylinder::closed::set)
                        min?.let(cylinder::min::set)
                        max?.let(cylinder::max::set)
                        world.shapes += cylinder
                    }
                }
            }
        }

        return Pair(world, camera)
    }

    override fun toString(): String {
        return "SceneLoader(" +
            "colorDefinitions=$colorDefinitions, " +
            "materialDefinitions=$materialDefinitions, " +
            "transformationDefinitions=$transformationDefinitions" +
            ")"
    }

    companion object {
        fun parseYamlToScene(path: String): Pair<World, Camera> {
            val file = File(path)
            val objectMapper = ObjectMapper(YAMLFactory())
            val yaml = objectMapper.readTree(file.inputStream())
            val sceneLoader = SceneLoader()
            sceneLoader.processDefinitions(yaml)
            return sceneLoader.parseScene(yaml)
        }
    }
}
