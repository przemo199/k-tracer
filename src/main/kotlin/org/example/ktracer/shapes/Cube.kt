package org.example.ktracer.shapes

import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.max
import org.example.ktracer.EPSILON
import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.MAX
import org.example.ktracer.primitives.Transformation

class Cube(
    material: Material = Material(),
    transformation: Transformation = Transformation.IDENTITY
) : Shape(material, transformation) {
    override fun localNormalAt(point: Point): Vector {
        val absPoint = point.abs()

        return when (absPoint.max()) {
            absPoint.x -> Vector(x = point.x)
            absPoint.y -> Vector(y = point.y)
            else -> Vector(z = point.z)
        }
    }

    override fun localIntersect(ray: Ray, intersections: Intersections) {
        val (xDistanceMin, xDistanceMax) = checkAxis(ray.origin.x, ray.direction.x, AXIS_MIN, AXIS_MAX)
        val (yDistanceMin, yDistanceMax) = checkAxis(ray.origin.y, ray.direction.y, AXIS_MIN, AXIS_MAX)
        val (zDistanceMin, zDistanceMax) = checkAxis(ray.origin.z, ray.direction.z, AXIS_MIN, AXIS_MAX)

        val distanceMin = max(max(xDistanceMin, yDistanceMin), zDistanceMin)
        val distanceMax = min(min(xDistanceMax, yDistanceMax), zDistanceMax)

        if (distanceMin < distanceMax && distanceMax >= 0) {
            intersections += Intersection(distanceMin, this)
            intersections += Intersection(distanceMax, this)
        }
    }

    override fun boundingBox(): BoundingBox {
        return BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is Cube && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Cube(material=$material, transformation=$transformation)"
    }

    companion object {
        private const val AXIS_MIN = -1.0

        private const val AXIS_MAX = 1.0

        @JvmStatic
        fun checkAxis(origin: Double, direction: Double, min: Double, max: Double): Pair<Double, Double> {
            val distanceMinNumerator = min - origin
            val distanceMaxNumerator = max - origin

            var distanceMin: Double
            var distanceMax: Double

            if (direction.absoluteValue >= EPSILON) {
                distanceMin = distanceMinNumerator / direction
                distanceMax = distanceMaxNumerator / direction
            } else {
                distanceMin = distanceMinNumerator * MAX
                distanceMax = distanceMaxNumerator * MAX
            }

            if (distanceMin > distanceMax) {
                distanceMin = distanceMax.also { distanceMax = distanceMin }
            }

            return Pair(distanceMin, distanceMax)
        }
    }
}
