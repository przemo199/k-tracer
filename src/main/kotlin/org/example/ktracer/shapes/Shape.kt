package org.example.ktracer.shapes

import java.util.Objects
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Vector
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation

abstract class Shape {
    var material: Material
    var transformation: Transformation
        get() {
            return transformationInverse.inverse()
        }
        set(value) {
            transformationInverse = value.inverse()
        }
    var transformationInverse: Transformation
        private set
    var parent: Shape? = null

    constructor(material: Material, transformation: Transformation) {
        this.material = material
        this.transformationInverse = transformation.inverse()
    }

    fun normalAt(worldPoint: Point): Vector {
        val localPoint = worldToObject(worldPoint)
        val localNormal = localNormalAt(localPoint)
        return normalToWorld(localNormal)
    }

    fun worldToObject(point: Point): Point {
        var localPoint = point
        parent?.let {
            localPoint = it.worldToObject(point)
        }
        return transformationInverse * localPoint
    }

    fun normalToWorld(normal: Vector): Vector {
        val worldNormal = (transformationInverse.transpose() * normal).normalized()

        parent?.let {
            return it.normalToWorld(worldNormal)
        }

        return worldNormal
    }

    open operator fun contains(other: Shape): Boolean {
        return this == other
    }

    fun parentSpaceBounds(): BoundingBox {
        return boundingBox().transform(transformation)
    }

    open fun divide(threshold: Int) {
        // noop for most of the shapes
    }

    abstract fun localNormalAt(point: Point): Vector

    abstract fun localIntersect(ray: Ray): Intersections?

    abstract fun boundingBox(): BoundingBox

    override fun equals(other: Any?): Boolean {
        return other is Shape &&
            material == other.material &&
            transformationInverse == other.transformationInverse &&
            parent == other.parent
    }

    override fun hashCode(): Int {
        return Objects.hash(material, transformationInverse, parent)
    }

    abstract override fun toString(): String
}
