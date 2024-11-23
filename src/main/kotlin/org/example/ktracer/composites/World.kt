package org.example.ktracer.composites

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.shapes.Shape
import org.example.ktracer.squared
import kotlin.math.sqrt

class World(var lights: MutableList<Light>, var shapes: MutableList<Shape>) {
    fun collectIntersections(ray: Ray, intersections: Intersections) {
        intersections.clear()
        shapes.forEach { ray.intersect(it, intersections) }
        intersections.sort()
    }

    fun shadeHit(computedHit: ComputedHit, intersections: Intersections, remainingIterations: Int): Color {
        val material = computedHit.shape.material
        val surfaceColor = lights.map {
            val inShadow = isShadowed(computedHit.overPoint, it, intersections)
            material.calculateLighting(computedHit, it, inShadow)
        }.fold(Color(0, 0, 0), Color::plus)

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
        collectIntersections(ray, intersections)

        intersections.hit()?.let {
            val computedHit = it.prepareComputations(ray, intersections)
            return shadeHit(computedHit, intersections, remainingIterations)
        }
        return DEFAULT_COLOR
    }

    fun colorAt(ray: Ray, intersections: Intersections = Intersections()): Color {
        return localColorAt(ray, intersections, MAX_REFLECTION_ITERATIONS)
    }

    fun isShadowed(point: Point, light: Light, intersections: Intersections): Boolean {
        val pointToLightDirection = light.position - point
        val distanceToLight = pointToLightDirection.magnitude
        val shadowRay = Ray(point, pointToLightDirection.normalized())
        collectIntersections(shadowRay, intersections)
        return intersections.any { it.shape.material.castsShadow && it.isWithinDistance(distanceToLight) }
    }

    fun reflectedColor(computedHit: ComputedHit, intersections: Intersections, remainingIterations: Int): Color {
        if (remainingIterations == 0 || computedHit.shape.material.reflectiveness == 0.0) {
            return DEFAULT_COLOR
        }

        val reflectedRay = Ray(computedHit.overPoint, computedHit.reflectionDirection)
        val reflectedColor = localColorAt(reflectedRay, intersections, remainingIterations - 1)
        return reflectedColor * computedHit.shape.material.reflectiveness
    }

    fun refractedColor(computedHit: ComputedHit, intersections: Intersections, remainingIterations: Int): Color {
        if (remainingIterations == 0 || computedHit.shape.material.transparency == 0.0) {
            return DEFAULT_COLOR
        }

        val nRatio = computedHit.refractiveIndex1 / computedHit.refractiveIndex2
        val cosI = computedHit.cameraDirection dot computedHit.normal
        val sin2t = nRatio.squared() * (1.0 - cosI.squared())
        val isTotalInternalReflection = sin2t > 1.0

        if (isTotalInternalReflection) {
            return DEFAULT_COLOR
        }

        val cosT = sqrt(1.0 - sin2t)
        val direction = (computedHit.normal * ((nRatio * cosI) - cosT)) - (computedHit.cameraDirection * nRatio)
        val refractedRay = Ray(computedHit.underPoint, direction)
        val refractedColor = localColorAt(refractedRay, intersections, remainingIterations - 1)
        return refractedColor * computedHit.shape.material.transparency
    }

    override fun toString(): String {
        return "World(lights=${arrayOf(lights).contentDeepToString()}, shapes=${arrayOf(shapes).contentDeepToString()})"
    }

    companion object {
        const val MAX_REFLECTION_ITERATIONS = 6

        @JvmField
        val DEFAULT_COLOR = Color.BLACK
    }
}
