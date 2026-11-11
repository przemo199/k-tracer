package org.example.ktracer.composites

import org.example.ktracer.coarseEquals
import org.example.ktracer.shapes.Shape

data class Intersection(val distance: Double, val shape: Shape) : Comparable<Intersection> {
    fun prepareComputations(ray: Ray, intersections: Intersections): ComputedHit {
        val point = ray.position(distance)
        var normal = shape.normalAt(point)
        val camera = -ray.direction
        val isInside = (normal dot camera) < 0.0

        if (isInside) {
            normal = -normal
        }

        val reflectionDirection = ray.direction.reflect(normal)

        val containers: MutableList<Shape> = mutableListOf()
        var refractiveIndex1 = Material.DEFAULT_REFRACTIVE_INDEX
        var refractiveIndex2 = Material.DEFAULT_REFRACTIVE_INDEX
        for (intersection in intersections) {
            val isThis = intersection === this
            if (isThis) {
                refractiveIndex1 = getRefrctiveIndexFromContainers(containers)
            }

            if (!containers.remove(intersection.shape)) {
                containers += intersection.shape
            }

            if (isThis) {
                refractiveIndex2 = getRefrctiveIndexFromContainers(containers)
                break
            }
        }

        return ComputedHit(
            distance,
            shape,
            point,
            camera,
            normal,
            reflectionDirection,
            refractiveIndex1,
            refractiveIndex2,
            isInside,
        )
    }

    private fun getRefrctiveIndexFromContainers(containers: List<Shape>): Double {
        return if (containers.isEmpty()) Material.DEFAULT_REFRACTIVE_INDEX else containers.last().material.refractiveIndex
    }

    fun isWithinDistance(distance: Double): Boolean = this.distance in 0.0..<distance

    override fun compareTo(other: Intersection): Int = distance.compareTo(other.distance)

    override fun equals(other: Any?): Boolean {
        return this === other ||
                other is Intersection &&
                distance coarseEquals other.distance &&
                shape == other.shape
    }

    override fun toString(): String = "Intersection(distance=$distance, shape=$shape)"

    companion {
        operator fun invoke(distance: Number, shape: Shape): Intersection = Intersection(distance.toDouble(), shape)
    }
}
