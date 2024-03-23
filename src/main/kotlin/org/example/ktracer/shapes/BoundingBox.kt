package org.example.ktracer.shapes

import java.util.Objects
import kotlin.math.absoluteValue
import org.example.ktracer.MAX
import org.example.ktracer.MIN
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

class BoundingBox(
    var min: Point = Point(MAX, MAX, MAX),
    var max: Point = Point(MIN, MIN, MIN),
) {
    constructor(shapes: Iterable<Shape>) : this() {
        shapes.map(Shape::parentSpaceBounds).forEach(::add)
    }

    fun transform(transformation: Transformation): BoundingBox {
        val points = listOf(
            min,
            Point(min.x, min.y, max.z),
            Point(min.x, max.y, min.z),
            Point(min.x, max.y, max.z),
            Point(max.x, min.y, min.z),
            Point(max.x, min.y, max.z),
            Point(max.x, max.y, min.z),
            max,
        )
        val boundingBox = BoundingBox()

        points.forEach { boundingBox.add(transformation * it) }

        return boundingBox
    }

    internal fun add(point: Point) {
        if (min.x > point.x) {
            min = min.copy(x = point.x)
        }

        if (min.y > point.y) {
            min = min.copy(y = point.y)
        }

        if (min.z > point.z) {
            min = min.copy(z = point.z)
        }

        if (max.x < point.x) {
            max = max.copy(x = point.x)
        }

        if (max.y < point.y) {
            max = max.copy(y = point.y)
        }

        if (max.z < point.z) {
            max = max.copy(z = point.z)
        }
    }

    fun add(box: BoundingBox) {
        add(box.min)
        add(box.max)
    }

    fun add(shape: Shape) {
        add(shape.boundingBox())
    }

    internal fun intersects(ray: Ray): Boolean {
        val (xDistanceMin, xDistanceMax) = Cube.checkAxis(ray.origin.x, ray.direction.x, min.x, max.x)
        val (yDistanceMin, yDistanceMax) = Cube.checkAxis(ray.origin.y, ray.direction.y, min.y, max.y)
        val (zDistanceMin, zDistanceMax) = Cube.checkAxis(ray.origin.z, ray.direction.z, min.z, max.z)

        val distanceMin = listOf(xDistanceMin, yDistanceMin, zDistanceMin).max()
        val distanceMax = listOf(xDistanceMax, yDistanceMax, zDistanceMax).min()

        return !(distanceMin > distanceMax || distanceMax < 0)
    }

    internal fun splitBounds(): Pair<BoundingBox, BoundingBox> {
        val dimensionX = (max.x - min.x).absoluteValue
        val dimensionY = (max.y - min.y).absoluteValue
        val dimensionZ = (max.z - min.z).absoluteValue

        val maxDimension = listOf(dimensionX, dimensionY, dimensionZ).max()

        var (xMin, yMin, zMin) = min
        var (xMax, yMax, zMax) = max

        when (maxDimension) {
            dimensionX -> {
                xMin += maxDimension / 2.0
                xMax = xMin
            }
            dimensionY -> {
                yMin += maxDimension / 2.0
                yMax = yMin
            }
            else -> {
                zMin += maxDimension / 2.0
                zMax = zMin
            }
        }

        val midMin = Point(xMin, yMin, zMin)
        val midMax = Point(xMax, yMax, zMax)

        val leftBoundingBox = BoundingBox(min, midMax)
        val rightBoundingBox = BoundingBox(midMin, max)

        return Pair(leftBoundingBox, rightBoundingBox)
    }

    operator fun contains(point: Point): Boolean {
        return min.x <= point.x && point.x <= max.x &&
            min.y <= point.y && point.y <= max.y &&
            min.z <= point.z && point.z <= max.z
    }

    operator fun contains(box: BoundingBox): Boolean {
        return box.min in this && box.max in this
    }

    override fun equals(other: Any?): Boolean {
        return other is BoundingBox &&
            min == other.min &&
            max == other.max
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), min, max)
    }

    override fun toString(): String {
        return "BoundingBox(min=$min, max=$max)"
    }
}
