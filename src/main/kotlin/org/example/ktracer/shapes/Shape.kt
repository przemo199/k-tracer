package org.example.ktracer.shapes

import java.util.Objects
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Vector
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

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
}

abstract class Shape(
    var material: Material,
    override var transformation: Transformation
) : Intersectable, Transformable, Boxable {
    var parent: Shape? = null

    fun normalAt(worldPoint: Point): Vector {
        return worldPoint.run(::worldToObject).run(::localNormalAt).run(::normalToWorld)
    }

    fun worldToObject(point: Point): Point {
        return transformation.inverse() * (parent?.worldToObject(point) ?: point)
    }

    fun normalToWorld(normal: Vector): Vector {
        val worldNormal = (transformation.inverse().transpose() * normal).normalized()
        return parent?.normalToWorld(worldNormal) ?: worldNormal
    }

    open operator fun contains(other: Shape): Boolean {
        return this == other
    }

    override fun parentSpaceBounds(): BoundingBox {
        return boundingBox().transform(transformation)
    }

    abstract fun localNormalAt(point: Point): Vector

    override fun equals(other: Any?): Boolean {
        return other is Shape &&
                material == other.material &&
                transformation == other.transformation &&
                parent == other.parent
    }

    override fun hashCode(): Int {
        return Objects.hash(material, transformation, parent)
    }

    abstract override fun toString(): String
}
