package org.example.ktracer.shapes

import java.util.Objects
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Vector

class ConstructiveSolidGeometry(
    val operand: Operand,
    val left: Shape,
    val right: Shape,
    material: Material = Material(),
    transformation: Transformation = Transformation.IDENTITY,
) : Shape(material, transformation) {
    enum class Operand {
        UNION,
        INTERSECTION,
        DIFFERENCE,
    }

    internal fun filterIntersections(intersections: Intersections): Intersections {
        val result = Intersections()
        var inLeft = false
        var inRight = false
        intersections.forEach {
            val leftHit = left in it.shape

            if (intersectionAllowed(operand, leftHit, inLeft, inRight)) {
                result += it
            }

            if (leftHit) inLeft = !inLeft else inRight = !inRight
        }
        return result
    }

    override fun localNormalAt(point: Point): Vector {
        throw UnsupportedOperationException()
    }

    override fun localIntersect(ray: Ray, intersections: Intersections) {
        if (!boundingBox().intersects(ray)) {
            return
        }

        val localIntersections = Intersections()
        ray.intersect(left, localIntersections)
        ray.intersect(right, localIntersections)

        if (localIntersections.isEmpty()) {
            return
        }

        localIntersections.sort()
        intersections += filterIntersections(localIntersections)
    }

    override fun contains(other: Shape): Boolean {
        return left == other || right == other
    }

    override fun boundingBox(): BoundingBox {
        return BoundingBox().apply {
            add(left.parentSpaceBounds())
            add(right.parentSpaceBounds())
        }
    }

    override fun divide(threshold: Int) {
        left.divide(threshold)
        right.divide(threshold)
    }

    override fun equals(other: Any?): Boolean {
        return other is ConstructiveSolidGeometry &&
            super.equals(other) &&
            left == other.left &&
            right == other.right
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), left, right)
    }

    override fun toString(): String {
        return "ConstructiveSolidGeometry(operand=$operand, left=$left, right=$right)"
    }

    companion object {
        @JvmStatic
        internal fun intersectionAllowed(
            operand: Operand,
            leftHit: Boolean,
            inLeft: Boolean,
            inRight: Boolean,
        ): Boolean {
            return when (operand) {
                Operand.UNION -> (leftHit && !inRight) || (!leftHit && !inLeft)
                Operand.INTERSECTION -> (leftHit && inRight) || (!leftHit && inLeft)
                Operand.DIFFERENCE -> (leftHit && !inRight) || (!leftHit && inLeft)
            }
        }
    }
}
