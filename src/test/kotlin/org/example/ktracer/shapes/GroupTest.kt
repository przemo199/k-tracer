package org.example.ktracer.shapes

import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Matrix
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroupTest {
    @Test
    fun `creating group`() {
        val group = Group()
        assertEquals(Matrix.IDENTITY, group.transformation)
        assertTrue(group.children.isEmpty())
    }

    @Test
    fun `add child to a group`() {
        val group = Group()
        val sphere = Sphere()
        group.add(sphere)
        assertTrue(group.children.isNotEmpty())
        assertTrue(sphere in group.children)
        assertEquals(group, sphere.parent)
    }

    @Test
    fun `intersecting ray with an empty group`() {
        val group = Group()
        val ray = Ray(Point(0, 0, 0), Vector.FORWARD)
        assertTrue(ray.intersect(group).isNullOrEmpty())
    }

    @Test
    fun `intersecting ray with non-empty group`() {
        val group = Group()
        val sphere1 = Sphere()
        val sphere2 = Sphere()
        sphere2.transformation = Transformations.translation(0, 0, -3)
        val sphere3 = Sphere()
        sphere3.transformation = Transformations.translation(5, 0, 0)
        group.add(sphere1)
        group.add(sphere2)
        group.add(sphere3)
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val intersections = ray.intersect(group)!!
        assertEquals(4, intersections.size)
        assertEquals(sphere2, intersections[0].shape)
        assertEquals(sphere2, intersections[1].shape)
        assertEquals(sphere1, intersections[2].shape)
        assertEquals(sphere1, intersections[3].shape)
    }

    @Test
    fun `intersecting ray with transformed group`() {
        val group = Group()
        group.transformation = Transformations.scaling(2, 2, 2)
        val sphere = Sphere()
        sphere.transformation = Transformations.translation(5, 0, 0)
        group.add(sphere)
        val ray = Ray(Point(10, 0, -10), Vector.FORWARD)
        assertEquals(2, ray.intersect(group)!!.size)
    }

    @Test
    fun `converting point from world to object space`() {
        val group = Group()
        group.transformation = Transformations.rotationY(PI / 2.0)
        val group2 = Group()
        group2.transformation = Transformations.scaling(2, 2, 2)
        group.add(group2)
        val sphere = Sphere()
        sphere.transformation = Transformations.translation(5, 0, 0)
        group2.add(sphere)
        val point = sphere.worldToObject(Point(-2, 0, -10))
        assertEquals(Point(0, 0, -1), point)
    }

    @Test
    fun `converting normal from world to object space`() {
        val group = Group()
        group.transformation = Transformations.rotationY(PI / 2.0)
        val group2 = Group()
        group2.transformation = Transformations.scaling(1, 2, 3)
        group.add(group2)
        val sphere = Sphere()
        sphere.transformation = Transformations.translation(5, 0, 0)
        group2.add(sphere)
        val normal = sphere.normalToWorld(Vector(sqrt(3.0) / 3.0, sqrt(3.0) / 3.0, sqrt(3.0) / 3.0))
        assertEquals(Vector(0.28571428571428575, 0.42857142857142855, -0.8571428571428571), normal)
    }

    @Test
    fun `finding normal on a child shape`() {
        val group = Group()
        group.transformation = Transformations.rotationY(PI / 2.0)
        val group2 = Group()
        group2.transformation = Transformations.scaling(1, 2, 3)
        group.add(group2)
        val sphere = Sphere()
        sphere.transformation = Transformations.translation(5, 0, 0)
        group2.add(sphere)
        val normal = sphere.normalAt(Point(1.7321, 1.1547, -5.5774))
        assertEquals(Vector(0.28570368184140726, 0.42854315178114105, -0.8571605294481017), normal)
    }

    @Test
    fun `partitioning group's children`() {
        val sphere1 = Sphere()
        sphere1.transformation = Transformations.translation(-2, 0, 0)
        val sphere2 = Sphere()
        sphere2.transformation = Transformations.translation(2, 0, 0)
        val sphere3 = Sphere()
        val group = Group()
        group.add(sphere1)
        group.add(sphere2)
        group.add(sphere3)
        val (left, right) = group.partitionChildren()

        assertEquals(1, group.children.size)
        assertEquals(1, left.size)
        assertEquals(1, right.size)
        assertTrue(sphere3 in group)
        assertTrue(sphere1 in left)
        assertTrue(sphere2 in right)
    }

    @Test
    fun `creating subgroup from a list of children`() {
        val sphere1 = Sphere()
        val sphere2 = Sphere()
        val group = Group()
        group.addSubGroup(listOf(sphere1, sphere2))

        assertEquals(1, group.children.size)
        assertTrue(sphere1 in group.children.first())
        assertTrue(sphere2 in group.children.first())
    }
}
