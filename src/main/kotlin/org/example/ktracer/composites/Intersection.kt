package org.example.ktracer.composites

import java.util.Objects
import org.example.ktracer.coarseEquals
import org.example.ktracer.shapes.Shape

class Intersection(distance: Number, val shape: Shape) {
    val distance = distance.toDouble()

    fun prepareComputations(ray: Ray, intersections: Intersections): ComputedHit {
        val point = ray.position(distance)
        var normal = shape.normalAt(point)
        val camera = -ray.direction
        val isInside = (normal dot camera) < 0.0

        if (isInside) {
            normal = -normal
        }

        val reflectionVector = ray.direction.reflect(normal)

        val containers: MutableList<Shape> = mutableListOf()
        var refractiveIndex1 = 1.0
        var refractiveIndex2 = 1.0
        for (intersection in intersections) {
            if (intersection == this) {
                refractiveIndex1 = if (containers.isEmpty()) 1.0 else containers.last().material.refractiveIndex
            }

            if (containers.contains(intersection.shape)) {
                containers.remove(intersection.shape)
            } else {
                containers.add(intersection.shape)
            }

            if (intersection == this) {
                refractiveIndex2 = if (containers.isEmpty()) 1.0 else containers.last().material.refractiveIndex
                break
            }
        }

        return ComputedHit(
            distance,
            shape,
            point,
            camera,
            normal,
            reflectionVector,
            isInside,
            refractiveIndex1,
            refractiveIndex2,
        )
    }

    fun isWithinDistance(distance: Number): Boolean {
        return this.distance >= 0.0 && this.distance < distance.toDouble()
    }

    override fun equals(other: Any?): Boolean {
        return other is Intersection &&
            distance coarseEquals other.distance &&
            shape == other.shape
    }

    override fun hashCode(): Int {
        return Objects.hash(distance, shape)
    }

    override fun toString(): String {
        return "Intersection(distance=$distance, shape=$shape)"
    }
}
