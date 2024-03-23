package org.example.ktracer.shapes

import org.example.ktracer.EPSILON
import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import kotlin.math.absoluteValue
import org.example.ktracer.MAX
import org.example.ktracer.primitives.Transformation

class Cube(material: Material = Material(), transformation: Transformation = Transformation.IDENTITY) :
    Shape(material, transformation) {

    override fun localNormalAt(point: Point): Vector {
        val maxValue = listOf(point.x.absoluteValue, point.y.absoluteValue, point.z.absoluteValue).max()

        if (maxValue == point.x.absoluteValue) {
            return Vector(point.x, 0, 0)
        } else if (maxValue == point.y.absoluteValue) {
            return Vector(0, point.y, 0)
        }
        return Vector(0, 0, point.z)
    }

    override fun localIntersect(ray: Ray): Intersections? {
        val (xDistanceMin, xDistanceMax) = checkAxis(ray.origin.x, ray.direction.x, AXIS_MIN, AXIS_MAX)
        val (yDistanceMin, yDistanceMax) = checkAxis(ray.origin.y, ray.direction.y, AXIS_MIN, AXIS_MAX)
        val (zDistanceMin, zDistanceMax) = checkAxis(ray.origin.z, ray.direction.z, AXIS_MIN, AXIS_MAX)

        val distanceMin = listOf(xDistanceMin, yDistanceMin, zDistanceMin).max()
        val distanceMax = listOf(xDistanceMax, yDistanceMax, zDistanceMax).min()

        if (distanceMin > distanceMax || distanceMax < 0) {
            return null
        } else {
            return Intersections(Intersection(distanceMin, this), Intersection(distanceMax, this))
        }
    }

    override fun boundingBox(): BoundingBox {
        return BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    }

    override fun equals(other: Any?): Boolean {
        return other is Cube &&
            super.equals(other)
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
