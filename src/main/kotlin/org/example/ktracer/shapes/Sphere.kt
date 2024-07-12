package org.example.ktracer.shapes

import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Vector
import org.example.ktracer.solveQuadratic

class Sphere(
    material: Material = Material(),
    transformation: Transformation = Transformation.IDENTITY
) : Shape(material, transformation) {
    override fun localNormalAt(point: Point): Vector {
        return Vector(point.x, point.y, point.z)
    }

    override fun localIntersect(ray: Ray, intersections: Intersections) {
        val sphereToRayDistance = Vector(ray.origin.x, ray.origin.y, ray.origin.z)
        val a = ray.direction dot ray.direction
        val b = 2.0 * (ray.direction dot sphereToRayDistance)
        val c = (sphereToRayDistance dot sphereToRayDistance) - 1.0

        solveQuadratic(a, b, c)?.let {
            intersections += Intersection(it.first, this)
            intersections += Intersection(it.second, this)
        }
    }

    override fun boundingBox(): BoundingBox {
        return BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is Sphere && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Sphere(material=$material, transformation=$transformation)"
    }
}
