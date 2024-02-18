package org.example.ktracer.shapes

import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Material
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Vector

class Group(material: Material = Material(), transformation: Transformation = Transformation.IDENTITY) :
    Shape(material, transformation) {
    val children = mutableListOf<Shape>()

    fun add(shape: Shape) {
        shape.parent = this
        children.add(shape)
    }

    override fun localNormalAt(point: Point): Vector {
        throw UnsupportedOperationException()
    }

    override fun localIntersect(ray: Ray): Intersections? {
        if (!boundingBox().intersects(ray)) {
            return null
        }

        val intersections = Intersections()
        for (shape in children) {
            ray.intersect(shape)?.let {
                intersections.addAll(it)
            }
        }
        intersections.sortByDistance()
        return if (intersections.isEmpty()) null else intersections
    }

    override fun contains(other: Shape): Boolean {
        return children.any { other in it }
    }

    override fun boundingBox(): BoundingBox {
        return BoundingBox(children)
    }

    fun partitionChildren(): Pair<List<Shape>, List<Shape>> {
        val (leftBox, rightBox) = parentSpaceBounds().splitBounds()
        val leftGroup = mutableListOf<Shape>()
        val rightGroup = mutableListOf<Shape>()
        children.forEach {
            val childBoundingBox = it.parentSpaceBounds()
            if (childBoundingBox in leftBox) {
                if (childBoundingBox !in rightBox) {
                    leftGroup.add(it)
                }
            } else {
                if (childBoundingBox in rightBox) {
                    rightGroup.add(it)
                }
            }
        }
        leftGroup.forEach(children::remove)
        rightGroup.forEach(children::remove)
        return Pair(leftGroup, rightGroup)
    }

    fun addSubGroup(shapes: List<Shape>) {
        if (shapes.isNotEmpty()) {
            val group = Group()
            shapes.forEach(group::add)
            add(group)
        }
    }

    override fun divide(threshold: Int) {
        if (children.size >= threshold) {
            val (left, right) = partitionChildren()
            addSubGroup(left)
            addSubGroup(right)
        }

        children.forEach {
            it.divide(threshold)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Group &&
            super.equals(other) &&
            children == other.children
    }

    override fun toString(): String {
        return "Group(elements=$children)"
    }
}
