package org.example.ktracer.composites

import org.example.ktracer.primitives.Matrix
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Shape

data class Ray(val origin: Point, val direction: Vector) {
    fun position(distance: Number): Point {
        return origin + (direction * distance.toDouble())
    }

    fun intersect(shape: Shape): Intersections? {
        val localRay = transform(shape.transformationInverse)
        return shape.localIntersect(localRay)
    }

    fun transform(transformation: Matrix): Ray {
        val newOrigin = transformation * origin
        val newDirection = transformation * direction
        return Ray(newOrigin, newDirection)
    }
}
