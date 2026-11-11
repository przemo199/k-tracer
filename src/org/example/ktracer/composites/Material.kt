package org.example.ktracer.composites

import java.util.Objects
import kotlin.math.pow
import org.example.ktracer.coarseEquals
import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Shape

data class Material(
    val color: Color = Color.WHITE,
    val ambient: Double = 0.1,
    val diffuse: Double = 0.9,
    val specular: Double = 0.9,
    val shininess: Double = 200.0,
    val reflectiveness: Double = 0.0,
    val refractiveIndex: Double = DEFAULT_REFRACTIVE_INDEX,
    val transparency: Double = 0.0,
    val castsShadow: Boolean = true,
    val colorProvider: ColorProvider? = null,
) {
    val isReflective get() = reflectiveness > 0.0
    val isUnreflective get() = reflectiveness == 0.0
    val isTransparent get() = transparency > 0.0
    val isOpaque get() = transparency == 0.0

    fun lighting(
        shape: Shape,
        light: Light,
        point: Point,
        cameraDirection: Vector,
        normal: Vector,
        inShadow: Boolean,
    ): Color {
        val color = colorProvider?.colorAtShape(shape, point) ?: color
        val effectiveColor = color * light.intensity
        val ambient = effectiveColor * ambient
        if (inShadow) {
            return ambient
        }

        val lightDirection = (light.position - point).normalized()
        val lightDotNormal = (lightDirection dot normal)
        if (lightDotNormal < 0.0) {
            return ambient
        }
        val diffuse = effectiveColor * this.diffuse * lightDotNormal
        val reflectionDirection = -(lightDirection).reflect(normal)
        val reflectDotCamera = reflectionDirection dot cameraDirection
        val specular = if (reflectDotCamera <= 0.0) {
            Color.BLACK
        } else {
            light.intensity * this.specular * reflectDotCamera.pow(shininess)
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

    companion {
        /**
         * Refractive index of air
         */
        const val DEFAULT_REFRACTIVE_INDEX = 1.0

        val GLASS
            get() = Material(transparency = 1.0, refractiveIndex = 1.5)
    }
}
