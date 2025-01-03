package org.example.ktracer.shapes

import java.util.Objects
import org.example.ktracer.EPSILON
import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import kotlin.math.absoluteValue
import org.example.ktracer.primitives.Transformation

open class Triangle(
    val vertex1: Point,
    val vertex2: Point,
    val vertex3: Point,
    material: Material = Material(),
    transformation: Transformation = Transformation.IDENTITY,
) : Shape(material, transformation) {
    val edge1: Vector = vertex2 - vertex1
    val edge2: Vector = vertex3 - vertex1
    val normal: Vector = (edge2 cross edge1).normalized()

    override fun localNormalAt(point: Point): Vector {
        return normal
    }

    override fun localIntersect(ray: Ray, intersections: Intersections) {
        val directionCrossEdge2 = ray.direction cross edge2
        val determinant = edge1 dot directionCrossEdge2
        if (determinant.absoluteValue < EPSILON) {
            return
        }

        val determinantInverse = 1.0 / determinant
        val vertexToOrigin = ray.origin - vertex1
        val u = determinantInverse * (vertexToOrigin dot directionCrossEdge2)
        if (u !in 0.0..1.0) {
            return
        }
        val originCrossEdge1 = vertexToOrigin cross edge1
        val v = determinantInverse * (ray.direction dot originCrossEdge1)
        if (v < 0.0 || u + v > 1.0) {
            return
        }
        val distance = determinantInverse * (edge2 dot originCrossEdge1)
        intersections += Intersection(distance, this)
    }

    override fun boundingBox(): BoundingBox {
        return BoundingBox().apply {
            add(vertex1)
            add(vertex2)
            add(vertex3)
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is Triangle &&
            super.equals(other) &&
            vertex1 == other.vertex1 &&
            vertex2 == other.vertex2 &&
            vertex3 == other.vertex3 &&
            normal == other.normal
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), vertex1, vertex2, vertex3)
    }

    override fun toString(): String {
        return "Triangle(material=$material, " +
                "transformation=$transformation, " +
                "vertex1=$vertex1, " +
                "vertex2=$vertex2, " +
                "vertex3=$vertex3, " +
                "edge1=$edge1, " +
                "edge2=$edge2, " +
                "normal=$normal)"
    }
}
