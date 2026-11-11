package org.example.ktracer.scene

import com.charleskorn.kaml.Location
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Transformations

// Typed model of the scene YAML format (the one from *The Ray Tracer Challenge*).
//
// A scene is a list of entries. `add:` places a camera, light or shape; `define:` names a material, transform or
// color that other entries refer to by name. Every mapping rejects unknown keys (kotaml's strict mode), so typos are
// reported with their line and column instead of being ignored. `SceneResolver` turns these entries into a world.

sealed interface Entry

/** An `add:` entry: something placed in the scene, as opposed to a `define:`. */
sealed interface AddEntry : Entry

@Serializable
data class DefineEntry(
    val define: String,
    /** The material this one is layered over. Only materials can extend. */
    val extend: String? = null,
    val value: DefineValue,
) : Entry

@Serializable
data class CameraEntry(
    val add: String,
    val width: Int,
    val height: Int,
    @SerialName("field-of-view") val fieldOfView: Double,
    val from: Triplet,
    val to: Triplet,
    val up: Triplet,
) : AddEntry

@Serializable
data class LightEntry(
    val add: String,
    val at: Triplet,
    val intensity: Triplet,
) : AddEntry

@Serializable
enum class ShapeKind {
    @SerialName("plane") PLANE,
    @SerialName("sphere") SPHERE,
    @SerialName("cube") CUBE,
    @SerialName("cylinder") CYLINDER,
    @SerialName("cone") CONE,
}

@Serializable
data class ShapeEntry(
    val add: ShapeKind,
    val material: Ref<MaterialDef>? = null,
    val transform: List<Step> = emptyList(),
    /** `false` stops the shape from casting shadows, overriding the material's `casts-shadow`. */
    val shadow: Boolean? = null,
    /** Cylinders and cones only. */
    val min: Double? = null,
    val max: Double? = null,
    val closed: Boolean? = null,
) : AddEntry

/** Every field is optional, so that `extend` can layer a definition over its base. */
@Serializable
data class MaterialDef(
    val color: Ref<Triplet>? = null,
    val pattern: PatternDef? = null,
    val ambient: Double? = null,
    val diffuse: Double? = null,
    val specular: Double? = null,
    val shininess: Double? = null,
    val reflective: Double? = null,
    val transparency: Double? = null,
    @SerialName("refractive-index") val refractiveIndex: Double? = null,
    @SerialName("casts-shadow") val castsShadow: Boolean? = null,
) {
    /** This definition, with the fields it leaves out taken from [base]. */
    fun over(base: MaterialDef) = MaterialDef(
        color = color ?: base.color,
        pattern = pattern ?: base.pattern,
        ambient = ambient ?: base.ambient,
        diffuse = diffuse ?: base.diffuse,
        specular = specular ?: base.specular,
        shininess = shininess ?: base.shininess,
        reflective = reflective ?: base.reflective,
        transparency = transparency ?: base.transparency,
        refractiveIndex = refractiveIndex ?: base.refractiveIndex,
        castsShadow = castsShadow ?: base.castsShadow,
    )
}

@Serializable
enum class PatternType {
    @SerialName("stripes") STRIPES,
    @SerialName("gradient") GRADIENT,
    @SerialName("rings") RINGS,
    @SerialName("checkers") CHECKERS,
}

@Serializable
data class PatternDef(
    val type: PatternType,
    val colors: ColorPair,
    val transform: List<Step> = emptyList(),
)

/** A value written inline, or the name of a `define`d one. */
@Serializable(with = RefSerializer::class)
sealed interface Ref<out T> {
    data class Named(val name: String, val location: Location) : Ref<Nothing>

    data class Inline<T>(val value: T) : Ref<T>
}

/** A transform step: the name of a defined transform, or an operation such as `[scale, 1, 2, 3]`. */
typealias Step = Ref<Operation>

/** A single transform operation, already turned into its (always invertible) matrix. */
@Serializable(with = OperationSerializer::class)
class Operation(val matrix: Transformation)

/** Three numbers: a point, a vector or a color. */
@Serializable(with = TripletSerializer::class)
data class Triplet(val x: Double, val y: Double, val z: Double)

@Serializable(with = ColorPairSerializer::class)
data class ColorPair(val first: Ref<Triplet>, val second: Ref<Triplet>)

/** What a `define` names; told apart by its shape: a mapping, a list of steps, or three numbers. */
@Serializable(with = DefineValueSerializer::class)
sealed interface DefineValue {
    data class Material(val material: MaterialDef) : DefineValue

    data class Transform(val steps: List<Step>) : DefineValue

    data class Color(val color: Triplet) : DefineValue
}

private val ENTRY_KINDS = listOf("camera", "light") + ShapeKind.serializer().descriptor.elementNames

/** Reads one entry of the scene list, choosing its type from its `add:` or `define:` key. */
fun readEntry(node: YamlNode, yaml: Yaml): Entry {
    val map = node as? YamlMap ?: throw YamlException("A scene entry must be a mapping with `add` or `define`.", node.path)
    val deserializer: DeserializationStrategy<Entry> = when {
        map.getKey("define") != null -> DefineEntry.serializer()
        else -> {
            val key = map.getKey("add") ?: throw YamlException("A scene entry needs an `add` or a `define` key.", node.path)
            when (val kind = map.getScalar("add")?.content) {
                "camera" -> CameraEntry.serializer()
                "light" -> LightEntry.serializer()
                in ENTRY_KINDS -> ShapeEntry.serializer()
                else -> throw YamlException("Unknown `add: $kind`. Known kinds are: ${ENTRY_KINDS.joinToString()}", key.path)
            }
        }
    }
    return yaml.decodeFromYamlNode(deserializer, node)
}

/**
 * Base for the types that a plain data class can't describe, such as "a name or an inline value". The contextual
 * descriptor makes kotaml hand over the raw node, whatever its kind; [decode] then inspects it.
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
abstract class NodeSerializer<T>(name: String) : KSerializer<T> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor(name, SerialKind.CONTEXTUAL)

    abstract fun decode(node: YamlNode, yaml: Yaml): T

    final override fun deserialize(decoder: Decoder): T {
        val input = decoder as? YamlInput ?: throw SerializationException("Scenes can only be read from YAML.")
        return decode(input.node, input.yaml)
    }

    final override fun serialize(encoder: Encoder, value: T) =
        throw UnsupportedOperationException("Scenes can only be read.")
}

class RefSerializer<T>(private val valueSerializer: KSerializer<T>) :
    NodeSerializer<Ref<T>>("Ref<${valueSerializer.descriptor.serialName}>") {
    override fun decode(node: YamlNode, yaml: Yaml): Ref<T> = when (node) {
        is YamlScalar -> Ref.Named(node.content, node.location)
        else -> Ref.Inline(yaml.decodeFromYamlNode(valueSerializer, node))
    }
}

object TripletSerializer : NodeSerializer<Triplet>("Triplet") {
    override fun decode(node: YamlNode, yaml: Yaml): Triplet {
        val (x, y, z) = numbers(node, 3, "[x, y, z]")
        return Triplet(x, y, z)
    }
}

object ColorPairSerializer : NodeSerializer<ColorPair>("ColorPair") {
    private val colorSerializer = RefSerializer(TripletSerializer)

    override fun decode(node: YamlNode, yaml: Yaml): ColorPair {
        val items = (node as? YamlList)?.items
        if (items?.size != 2) {
            throw YamlException("Expected two colors, each a defined name or [r, g, b].", node.path)
        }
        return ColorPair(colorSerializer.decode(items[0], yaml), colorSerializer.decode(items[1], yaml))
    }
}

object OperationSerializer : NodeSerializer<Operation>("Operation") {
    private const val USAGE = "[scale|translate, x, y, z] or [rotate-x|rotate-y|rotate-z, radians]"

    override fun decode(node: YamlNode, yaml: Yaml): Operation {
        val first = (node as? YamlList)?.items?.firstOrNull() as? YamlScalar
            ?: throw YamlException("Expected a transform operation: $USAGE.", node.path)
        val name = first.content
        val arity = when (name) {
            "scale", "translate" -> 3
            "rotate-x", "rotate-y", "rotate-z" -> 1
            else -> throw YamlException("Unknown transform operation `$name`; expected $USAGE.", first.path)
        }
        val arguments = numbers(node, arity + 1, "[$name, ${if (arity == 3) "x, y, z" else "radians"}]", from = 1)
        val matrix = when (name) {
            "scale" -> Transformations.scaling(arguments[0], arguments[1], arguments[2])
            "translate" -> Transformations.translation(arguments[0], arguments[1], arguments[2])
            "rotate-x" -> Transformations.rotationX(arguments[0])
            "rotate-y" -> Transformations.rotationY(arguments[0])
            else -> Transformations.rotationZ(arguments[0])
        }
        // shapes and patterns use the inverse, so a singular matrix would break the render
        if (!matrix.isInvertible()) {
            throw YamlException("Transform `$name` is not invertible (is something scaled by 0?).", node.path)
        }
        return Operation(matrix)
    }
}

object DefineValueSerializer : NodeSerializer<DefineValue>("DefineValue") {
    private val stepSerializer = RefSerializer(OperationSerializer)

    override fun decode(node: YamlNode, yaml: Yaml): DefineValue = when {
        node is YamlMap -> DefineValue.Material(yaml.decodeFromYamlNode(MaterialDef.serializer(), node))
        node is YamlList && node.items.isNotEmpty() && node.items.all { it.isNumber() } ->
            DefineValue.Color(TripletSerializer.decode(node, yaml))
        node is YamlList && node.items.none { it.isNumber() } ->
            DefineValue.Transform(node.items.map { stepSerializer.decode(it, yaml) })
        else -> throw YamlException(
            "Expected a material (a mapping), a transform (a list of steps) or a color [r, g, b].",
            node.path,
        )
    }

    private fun YamlNode.isNumber() = this is YamlScalar && content.toDoubleOrNull() != null
}

/** Reads the numbers at indices `from until count` of a list that must have exactly [count] items. */
private fun numbers(node: YamlNode, count: Int, form: String, from: Int = 0): List<Double> {
    val items = (node as? YamlList)?.items
    if (items?.size != count) {
        throw YamlException("Expected $form.", node.path)
    }
    return items.drop(from).map { item ->
        (item as? YamlScalar)?.toDouble() ?: throw YamlException("Expected a number in $form.", item.path)
    }
}

/** Unlike [org.example.ktracer.primitives.Matrix.invertible], also rejects the NaN matrices that degenerate inputs produce. */
fun Transformation.isInvertible(): Boolean = determinant().let { it != 0.0 && it.isFinite() }
