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
    fun collect_intersections(ray: Ray, intersections: Intersections) {
        intersections.clear()
        shapes.forEach { ray.intersect(it, intersections) }
        intersections.sortByDistance()
    }

    fun shadeHit(computedHit: ComputedHit, intersections: Intersections, remainingIterations: Int): Color {
        val material = computedHit.shape.material
        val surfaceColor = lights.map {
            val inShadow = isShadowed(computedHit.overPoint, it, intersections)
            material.lightingFromComputedHit(computedHit, it, inShadow)
        }.fold(Color(0, 0, 0)) { acc, color -> acc + color }

        val reflectedColor = reflectedColor(computedHit, intersections, remainingIterations)
        val refractedColor = refractedColor(computedHit, intersections, remainingIterations)
        if (material.reflectiveness > 0.0 && material.transparency > 0.0) {
            val reflectance = computedHit.schlick()
            return surfaceColor + (reflectedColor * reflectance) + (refractedColor * (1.0 - reflectance))
        } else {
            return surfaceColor + reflectedColor + refractedColor
        }
    }

    private fun localColorAt(ray: Ray, intersections: Intersections, remainingIterations: Int): Color {
        collect_intersections(ray, intersections)

        intersections.hit()?.let {
            val computedHit = it.prepareComputations(ray, intersections)
            return shadeHit(computedHit, intersections, remainingIterations)
        }
        return Color.BLACK
    }

    fun colorAt(ray: Ray, intersections: Intersections): Color {
        return localColorAt(ray, intersections, MAX_REFLECTION_ITERATIONS)
    }

    fun isShadowed(point: Point, light: Light, intersections: Intersections): Boolean {
        val pointToLightVector = light.position - point
        val distanceToLight = pointToLightVector.magnitude
        val shadowRay = Ray(point, pointToLightVector.normalized())
        collect_intersections(shadowRay, intersections)
        return intersections.any { it.shape.material.castsShadow && it.isWithinDistance(distanceToLight) }
    }

    fun reflectedColor(computedHit: ComputedHit, intersections: Intersections, remainingIterations: Int): Color {
        if (remainingIterations == 0 || computedHit.shape.material.reflectiveness == 0.0) {
            return Color.BLACK
        }
        val reflectedRay = Ray(computedHit.overPoint, computedHit.reflectionVector)
        val reflectedColor = localColorAt(reflectedRay, intersections, remainingIterations - 1)
        return reflectedColor * computedHit.shape.material.reflectiveness
    }

    fun refractedColor(computedHit: ComputedHit, intersections: Intersections, remainingIterations: Int): Color {
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
        val refractedColor = localColorAt(refractedRay, intersections, remainingIterations - 1)
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
