package org.example.ktracer.composites

import org.example.ktracer.EPSILON
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Shape
import org.example.ktracer.squared
import kotlin.math.pow
import kotlin.math.sqrt

class ComputedHit(
    distance: Number,
    val shape: Shape,
    val point: Point,
    val cameraVector: Vector,
    val normal: Vector,
    val reflectionVector: Vector,
    val isInside: Boolean,
    refractiveIndex1: Number,
    refractiveIndex2: Number,
) {
    val distance = distance.toDouble()
    val refractiveIndex1 = refractiveIndex1.toDouble()
    val refractiveIndex2 = refractiveIndex2.toDouble()
    val overPoint = point + (normal * EPSILON)
    val underPoint = point - (normal * EPSILON)

    fun schlick(): Double {
        var cos = cameraVector dot normal

        if (refractiveIndex1 > refractiveIndex2) {
            val refractionRatio = refractiveIndex1 / refractiveIndex2
            val sin2t = refractionRatio.squared() * (1.0 - cos.squared())

            if (sin2t > 1.0) {
                return 1.0
            }

            cos = sqrt(1.0 - sin2t)
        }
        val r0 = ((refractiveIndex1 - refractiveIndex2) / (refractiveIndex1 + refractiveIndex2)).squared()
        return r0 + (1.0 - r0) * (1.0 - cos).pow(5)
    }
}
