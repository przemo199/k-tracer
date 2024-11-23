package org.example.ktracer.composites

import java.util.Objects
import org.example.ktracer.coarseEquals
import org.example.ktracer.composites.Material.Companion.DEFAULT_REFRACTIVE_INDEX
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
        var refractiveIndex1 = DEFAULT_REFRACTIVE_INDEX
        var refractiveIndex2 = DEFAULT_REFRACTIVE_INDEX
        for (intersection in intersections) {
            val isThis = intersection === this
            if (isThis) {
                refractiveIndex1 = if (containers.isEmpty()) DEFAULT_REFRACTIVE_INDEX else containers.last().material.refractiveIndex
            }

            if (!containers.remove(intersection.shape)) {
                containers += intersection.shape
            }

            if (isThis) {
                refractiveIndex2 = if (containers.isEmpty()) DEFAULT_REFRACTIVE_INDEX else containers.last().material.refractiveIndex
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

    fun isWithinDistance(distance: Double): Boolean {
        return this.distance >= 0.0 && this.distance < distance
    }

    override fun compareTo(other: Intersection): Int {
        return distance.compareTo(other.distance)
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is Intersection &&
            distance coarseEquals other.distance &&
            shape == other.shape
    }

    override fun hashCode(): Int {
        return Objects.hash(distance, shape)
    }

    override fun toString(): String {
        return "Intersection(distance=$distance, shape=$shape)"
    }

    companion object {
        operator fun invoke(distance: Number, shape: Shape): Intersection {
            return Intersection(distance.toDouble(), shape)
        }
    }
}
