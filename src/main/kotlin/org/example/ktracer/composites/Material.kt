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

data class Material(
    var color: Color = Color.WHITE,
    var ambient: Double = 0.1,
    var diffuse: Double = 0.9,
    var specular: Double = 0.9,
    var shininess: Double = 200.0,
    var reflectiveness: Double = 0.0,
    var refractiveIndex: Double = DEFAULT_REFRACTIVE_INDEX,
    var transparency: Double = 0.0,
    var castsShadow: Boolean = true,
    var pattern: Pattern? = null,
) {
    fun lighting(
        shape: Shape,
        light: Light,
        point: Point,
        cameraDirection: Vector,
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
        val lightDirection = (light.position - point).normalized()
        val lightDotNormal = (lightDirection dot normal)
        if (lightDotNormal < 0.0) {
            diffuse = Color.BLACK
            specular = Color.BLACK
        } else {
            diffuse = effectiveColor * this.diffuse * lightDotNormal
            val reflectionDirection = -(lightDirection).reflect(normal)
            val reflectDotCamera = reflectionDirection dot cameraDirection

            specular = if (reflectDotCamera <= 0.0) {
                Color.BLACK
            } else {
                light.intensity * this.specular * reflectDotCamera.pow(shininess)
            }
        }

        return ambient + diffuse + specular
    }

    fun calculateLighting(computedHit: ComputedHit, light: Light, inShadow: Boolean): Color {
        return lighting(
            computedHit.shape,
            light,
            computedHit.overPoint,
            computedHit.cameraDirection,
            computedHit.normal,
            inShadow,
        )
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is Material &&
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
        /**
         * Refractive index of air
         */
        const val DEFAULT_REFRACTIVE_INDEX = 1.0

        @JvmStatic
        val GLASS
            get() = Material(transparency = 1.0, refractiveIndex = 1.5)
    }
}
