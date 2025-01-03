package org.example.ktracer.composites

import org.example.ktracer.EPSILON
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Shape
import org.example.ktracer.squared
import kotlin.math.pow
import kotlin.math.sqrt

data class ComputedHit(
    val distance: Double,
    val shape: Shape,
    val point: Point,
    val cameraDirection: Vector,
    val normal: Vector,
    val reflectionDirection: Vector,
    val refractiveIndex1: Double,
    val refractiveIndex2: Double,
    val isInside: Boolean,
) {
    val overPoint = point + (normal * EPSILON)
    val underPoint = point - (normal * EPSILON)

    fun schlick(): Double {
        var cos = cameraDirection dot normal

        if (refractiveIndex1 > refractiveIndex2) {
            val refractionRatio = refractiveIndex1 / refractiveIndex2
            val sin2t = refractionRatio.squared() * (1.0 - cos.squared())

            if (sin2t > 1.0) {
                return 1.0
            }

            cos = sqrt(1.0 - sin2t)
        }
        val refractionCoefficient = ((refractiveIndex1 - refractiveIndex2) / (refractiveIndex1 + refractiveIndex2)).squared()
        return refractionCoefficient + ((1.0 - refractionCoefficient) * (1.0 - cos).pow(5))
    }

    companion object {
        operator fun invoke(
            distance: Number,
            shape: Shape,
            point: Point,
            cameraDirection: Vector,
            normal: Vector,
            reflectionDirection: Vector,
            refractiveIndex1: Number,
            refractiveIndex2: Number,
            isInside: Boolean,
        ): ComputedHit {
            return ComputedHit(
                distance.toDouble(),
                shape,
                point,
                cameraDirection,
                normal,
                reflectionDirection,
                refractiveIndex1.toDouble(),
                refractiveIndex2.toDouble(),
                isInside,
            )
        }
    }
}
