package org.example.ktracer.composites

import org.example.ktracer.MAX_REFLECTION_ITERATIONS
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Sphere
import org.example.ktracer.squared
import kotlin.math.sqrt

class World(var lights: MutableList<Light>, var shapes: MutableList<Shape>) {
    fun intersections(ray: Ray): Intersections {
        val intersections = Intersections()
        for (shape in shapes) {
            ray.intersect(shape)?.let {
                intersections.addAll(it)
            }
        }
        intersections.sortByDistance()
        return intersections
    }

    fun shadeHit(computedHit: ComputedHit, remainingIterations: Int): Color {
        val material = computedHit.shape.material
        val surfaceColor = lights.map {
            val inShadow = isShadowed(computedHit.overPoint, it)
            material.lightingFromComputedHit(computedHit, it, inShadow)
        }.fold(Color.BLACK) { acc, color -> acc + color }

        val reflectedColor = reflectedColor(computedHit, remainingIterations)
        val refractedColor = refractedColor(computedHit, remainingIterations)
        if (material.reflectiveness > 0.0 && material.transparency > 0.0) {
            val reflectance = computedHit.schlick()
            return surfaceColor + (reflectedColor * reflectance) + (refractedColor * (1.0 - reflectance))
        } else {
            return surfaceColor + reflectedColor + refractedColor
        }
    }

    private fun localColorAt(ray: Ray, remainingIterations: Int): Color {
        val intersections = intersections(ray)

        intersections.hit()?.let {
            val computedHit = it.prepareComputations(ray, intersections)
            return shadeHit(computedHit, remainingIterations)
        }
        return Color.BLACK
    }

    fun colorAt(ray: Ray): Color {
        return localColorAt(ray, MAX_REFLECTION_ITERATIONS)
    }

    fun isShadowed(point: Point, light: Light): Boolean {
        val pointToLightVector = light.position - point
        val distanceToLight = pointToLightVector.magnitude
        val shadowRay = Ray(point, pointToLightVector.normalized())
        val intersections = intersections(shadowRay)
        return intersections.stream().anyMatch { it.isWithinDistance(distanceToLight) && it.shape.material.castsShadow }
    }

    internal fun reflectedColor(computedHit: ComputedHit, remainingIterations: Int): Color {
        if (remainingIterations == 0 || computedHit.shape.material.reflectiveness == 0.0) {
            return Color.BLACK
        }
        val reflectedRay = Ray(computedHit.overPoint, computedHit.reflectionVector)
        val reflectedColor = localColorAt(reflectedRay, remainingIterations - 1)
        return reflectedColor * computedHit.shape.material.reflectiveness
    }

    internal fun refractedColor(computedHit: ComputedHit, remainingIterations: Int): Color {
        if (remainingIterations == 0 || computedHit.shape.material.transparency == 0.0) {
            return Color.BLACK
        }

        val nRatio = computedHit.refractiveIndex1 / computedHit.refractiveIndex2
        val cosI = computedHit.cameraVector dot computedHit.normal
        val sin2t = nRatio.squared() * (1.0 - cosI.squared())
        val isTotalInternalReflection = sin2t > 1.0

        if (isTotalInternalReflection) {
            return Color.BLACK
        }

        val cosT = sqrt(1.0 - sin2t)
        val direction = computedHit.normal * (nRatio * cosI - cosT) - (computedHit.cameraVector * nRatio)
        val refractedRay = Ray(computedHit.underPoint, direction)
        val refractedColor = localColorAt(refractedRay, remainingIterations - 1)
        return refractedColor * computedHit.shape.material.transparency
    }

    override fun toString(): String {
        return "World(lights=${arrayOf(lights).contentDeepToString()}, shapes=${arrayOf(shapes).contentDeepToString()})"
    }

    companion object {
        fun default(): World {
            val sphere1 = Sphere()
            sphere1.material.color = Color(0.8, 1, 0.6)
            sphere1.material.diffuse = 0.7
            sphere1.material.specular = 0.2
            val sphere2 = Sphere()
            sphere2.transformation = Transformations.scaling(0.5, 0.5, 0.5)
            return World(mutableListOf(Light()), mutableListOf(sphere1, sphere2))
        }
    }
}
