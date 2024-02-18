package org.example.ktracer.shapes

import kotlin.math.absoluteValue
import org.example.ktracer.EPSILON
import org.example.ktracer.MAX
import org.example.ktracer.MIN
import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Vector

class Plane(
    material: Material = Material(),
    transformation: Transformation = Transformation.IDENTITY,
) : Shape(material, transformation) {
    override fun localNormalAt(point: Point): Vector {
        return Vector.UP
    }

    override fun localIntersect(ray: Ray): Intersections? {
        if (ray.direction.y.absoluteValue < EPSILON) {
            return null
        }

        val distance = -ray.origin.y / ray.direction.y
        return Intersections(Intersection(distance, this))
    }

    override fun boundingBox(): BoundingBox {
        return BoundingBox(
            Point(MIN, 0, MIN),
            Point(MAX, 0, MAX),
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is Plane &&
            super.equals(other)
    }

    override fun toString(): String {
        return "Plane(material=$material, transformation=$transformation)"
    }
}
