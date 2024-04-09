package org.example.ktracer.composites

import java.util.Objects
import org.example.ktracer.coarseEquals
import org.example.ktracer.patterns.Pattern
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Shape
import kotlin.math.pow

class Material(
    var color: Color = Color.WHITE,
    ambient: Number = 0.1,
    diffuse: Number = 0.9,
    specular: Number = 0.9,
    shininess: Number = 200,
    reflectiveness: Number = 0,
    refractiveIndex: Number = 1,
    transparency: Number = 0,
    var castsShadow: Boolean = true,
    var pattern: Pattern? = null,
) {
    var ambient: Double = ambient.toDouble()
    var diffuse: Double = diffuse.toDouble()
    var specular: Double = specular.toDouble()
    var shininess: Double = shininess.toDouble()
    var reflectiveness: Double = reflectiveness.toDouble()
    var refractiveIndex: Double = refractiveIndex.toDouble()
    var transparency: Double = transparency.toDouble()

    constructor(material: Material) : this(
        material.color,
        material.ambient,
        material.diffuse,
        material.specular,
        material.shininess,
        material.reflectiveness,
        material.refractiveIndex,
        material.transparency,
        material.castsShadow,
        material.pattern,
    )

    fun lighting(
        shape: Shape,
        light: Light,
        point: Point,
        cameraVector: Vector,
        normal: Vector,
        inShadow: Boolean,
    ): Color {
        val color = pattern?.colorAtShape(shape, point) ?: color
        val effectiveColor = color * light.intensity
        val ambient = effectiveColor * ambient
        if (inShadow) {
            return ambient
        }

        val diffuse: Color
        val specular: Color
        val lightVector = (light.position - point).normalized()
        val lightDotNormal = (lightVector dot normal)
        if (lightDotNormal < 0.0) {
            diffuse = Color.BLACK
            specular = Color.BLACK
        } else {
            diffuse = effectiveColor * this.diffuse * lightDotNormal
            val reflectionVector = -(lightVector).reflect(normal)
            val reflectDotCamera = reflectionVector dot cameraVector

            specular = if (reflectDotCamera <= 0.0) {
                Color.BLACK
            } else {
                light.intensity * this.specular * reflectDotCamera.pow(shininess)
            }
        }

        return ambient + diffuse + specular
    }

    fun lightingFromComputedHit(computedHit: ComputedHit, light: Light, inShadow: Boolean): Color {
        return lighting(
            computedHit.shape,
            light,
            computedHit.overPoint,
            computedHit.cameraVector,
            computedHit.normal,
            inShadow,
        )
    }

    fun deepCopy(): Material {
        return Material(this)
    }

    override fun equals(other: Any?): Boolean {
        return other is Material &&
            color == other.color &&
            ambient coarseEquals other.ambient &&
            diffuse coarseEquals other.diffuse &&
            specular coarseEquals other.specular &&
            shininess coarseEquals other.shininess &&
            reflectiveness coarseEquals other.reflectiveness &&
            refractiveIndex coarseEquals other.refractiveIndex &&
            transparency coarseEquals other.transparency
    }

    override fun hashCode(): Int {
        return Objects.hash(
            color,
            ambient,
            diffuse,
            specular,
            shininess,
            reflectiveness,
            refractiveIndex,
            transparency
        )
    }

    override fun toString(): String {
        return "Material(color=$color, " +
                "ambient=$ambient, " +
                "diffuse=$diffuse, " +
                "specular=$specular, " +
                "shininess=$shininess, " +
                "reflectiveness=$reflectiveness, " +
                "refractiveIndex=$refractiveIndex, " +
                "transparency=$transparency, " +
                "castsShadow=$castsShadow)"
    }

    companion object {
        @JvmStatic
        val GLASS
            get() = Material(transparency = 1.0, refractiveIndex = 1.5)
    }
}
