package org.example.ktracer.shapes

import java.util.Objects
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Vector

interface Intersectable {
    fun localIntersect(ray: Ray, intersections: Intersections)
}

interface Transformable {
    val transformation: Transformation
}

interface Boxable {
    fun boundingBox(): BoundingBox

    fun divide(threshold: Int) {
        // noop for most of the shapes
    }

    fun parentSpaceBounds(): BoundingBox

    operator fun div(threshold: Int) = divide(threshold)
}

abstract class Shape(
    var material: Material,
    override var transformation: Transformation
) : Intersectable, Transformable, Boxable {
    var parent: Shape? = null

    fun normalAt(worldPoint: Point): Vector {
        val objectPoint = worldToObject(worldPoint)
        val localNormal = localNormalAt(objectPoint)
        return normalToWorld(localNormal)
    }

    fun worldToObject(point: Point): Point = transformation.inverse() * (parent?.worldToObject(point) ?: point)

    fun normalToWorld(normal: Vector): Vector {
        val worldNormal = (transformation.inverse().transpose() * normal).normalized()
        return parent?.normalToWorld(worldNormal) ?: worldNormal
    }

    open operator fun contains(other: Shape): Boolean = this == other

    override fun parentSpaceBounds(): BoundingBox = boundingBox().transform(transformation)

    abstract fun localNormalAt(point: Point): Vector

    override fun equals(other: Any?): Boolean {
        return other is Shape &&
                material == other.material &&
                transformation == other.transformation &&
                parent == other.parent
    }

    override fun hashCode(): Int = Objects.hash(material, transformation, parent)

    abstract override fun toString(): String
}
