package org.example.ktracer.shapes

import java.util.Objects
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import org.example.ktracer.EPSILON
import org.example.ktracer.MAX
import org.example.ktracer.MIN
import org.example.ktracer.coarseEquals
import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Vector
import org.example.ktracer.solveQuadratic
import org.example.ktracer.squared

class Cone(
    min: Number = MIN,
    max: Number = MAX,
    var closed: Boolean = false,
    material: Material = Material(),
    transformation: Transformation = Transformation.IDENTITY,
) : Shape(material, transformation) {
    var min: Double = min.toDouble()
    var max: Double = max.toDouble()

    private fun intersectCaps(ray: Ray, intersections: Intersections) {
        if (!closed || ray.direction.y.absoluteValue < EPSILON) {
            return
        }

        val distance1 = (min - ray.origin.y) / ray.direction.y
        if (checkCaps(ray, distance1, min)) {
            intersections += Intersection(distance1, this)
        }

        val distance2 = (max - ray.origin.y) / ray.direction.y
        if (checkCaps(ray, distance2, max)) {
            intersections += Intersection(distance2, this)
        }
    }

    override fun localNormalAt(point: Point): Vector {
        val distance = point.x.squared() + point.z.squared()

        if (distance < max.squared() && point.y >= (max - EPSILON)) {
            return Vector.UP
        }

        if (distance < min.squared() && point.y <= (min + EPSILON)) {
            return Vector.DOWN
        }

        var y = sqrt(distance)
        if (point.y > 0.0) {
            y = -y
        }

        return Vector(point.x, y, point.z)
    }

    override fun localIntersect(ray: Ray, intersections: Intersections) {
        val a = ray.direction.x.squared() - ray.direction.y.squared() + ray.direction.z.squared()
        val b = 2.0 * (
            (ray.origin.x * ray.direction.x) -
            (ray.origin.y * ray.direction.y) +
            (ray.origin.z * ray.direction.z)
        )
        val c = ray.origin.x.squared() - ray.origin.y.squared() + ray.origin.z.squared()

        if (a.absoluteValue < EPSILON && b.absoluteValue > EPSILON) {
            val distance = -c / (2.0 * b)
            intersections += Intersection(distance, this)
        } else {
            solveQuadratic(a, b, c)?.let {
                var distance1 = it.first
                var distance2 = it.second

                if (distance1 > distance2) {
                    distance1 = distance2.also { distance2 = distance1 }
                }

                val y1 = ray.origin.y + ray.direction.y * distance1
                if (min < y1 && y1 < max) {
                    intersections += Intersection(distance1, this)
                }

                val y2 = ray.origin.y + ray.direction.y * distance2
                if (min < y2 && y2 < max) {
                    intersections += Intersection(distance2, this)
                }
            }
        }

        intersectCaps(ray, intersections)
    }

    override fun boundingBox(): BoundingBox {
        val limit = maxOf(min.absoluteValue, max.absoluteValue)
        return BoundingBox(
            Point(-limit, min, -limit),
            Point(limit, max, limit)
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is Cone &&
            super.equals(other) &&
            closed == other.closed &&
            min coarseEquals other.min &&
            max coarseEquals other.max
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), closed, min, max)
    }

    override fun toString(): String {
        return "Cone(material=$material, transformation=$transformation, min=$min, max=$max, closed=$closed)"
    }

    companion object {
        @JvmStatic
        private fun checkCaps(ray: Ray, distance: Number, radius: Number): Boolean {
            distance.toDouble().also {
                val x = ray.origin.x + ray.direction.x * it
                val z = ray.origin.z + ray.direction.z * it
                return x.squared() + z.squared() <= radius.toDouble().squared()
            }
        }
    }
}
