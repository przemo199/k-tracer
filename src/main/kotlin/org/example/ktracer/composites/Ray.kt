package org.example.ktracer.composites

import org.example.ktracer.primitives.Matrix
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Intersectable
import org.example.ktracer.shapes.Transformable

data class Ray(val origin: Point, val direction: Vector) {
    fun position(distance: Number): Point {
        return origin + (direction * distance.toDouble())
    }

    fun <T> intersect(shape: T, intersections: Intersections) where T: Transformable, T: Intersectable {
        val localRay = transform(shape.transformationInverse)
        shape.localIntersect(localRay, intersections)
    }

    fun transform(transformation: Matrix): Ray {
        val newOrigin = transformation * origin
        val newDirection = transformation * direction
        return Ray(newOrigin, newDirection)
    }
}
