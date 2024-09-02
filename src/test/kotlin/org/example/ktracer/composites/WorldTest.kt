package org.example.ktracer.composites

import org.example.ktracer.patterns.TestPattern
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Light
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Plane
import org.example.ktracer.shapes.Sphere
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorldTest {
    @Test
    fun `default world`() {
        val world = World.default()
        val sphere1 = Sphere().apply {
            material.color = Color(0.8, 1.0, 0.6)
            material.diffuse = 0.7
            material.specular = 0.2
        }
        val sphere2 = Sphere()
        sphere2.transformation = Transformations.scaling(0.5, 0.5, 0.5)
        assertEquals(Light(), world.lights[0])
        assertTrue(sphere1 in world.shapes)
        assertTrue(sphere2 in world.shapes)
    }

    @Test
    fun `intersect world with ray`() {
        val world = World.default()
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val intersections = Intersections()
        world.collectIntersections(ray, intersections)
        intersections.sort()
        assertEquals(4, intersections.size)
        assertEquals(4.0, intersections[0].distance)
        assertEquals(4.5, intersections[1].distance)
        assertEquals(5.5, intersections[2].distance)
        assertEquals(6.0, intersections[3].distance)
    }

    @Test
    fun `shading intersection_from_inside`() {
        val world = World.default()
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val intersection = Intersection(4.0, world.shapes[0])
        val computedHit = intersection.prepareComputations(ray, Intersections())
        val color = world.shadeHit(computedHit, Intersections(), 1)
        assertEquals(Color(0.3806611928908177, 0.47582649111352215, 0.28549589466811326), color)
    }

    @Test
    fun `color when ray misses`() {
        val world = World.default()
        val ray = Ray(Point(0, 0, -5), Vector.UP)
        val color = world.colorAt(ray)
        assertEquals(Color.BLACK, color)
    }

    @Test
    fun `color when ray hits`() {
        val world = World.default()
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val color = world.colorAt(ray)
        assertEquals(Color(0.3806611928908177, 0.47582649111352215, 0.28549589466811326), color)
    }

    @Test
    fun `color with intersection behind ray`() {
        val world = World.default()

        val sphere1 = Sphere().apply {
            material.color = Color(0.8, 1, 0.6)
            material.diffuse = 0.7
            material.specular = 0.2
        }
        world.shapes[0] = sphere1

        val sphere2 = Sphere().apply {
            transformation = Transformations.scaling(0.5, 0.5, 0.5)
            material.ambient = 1.0
        }
        world.shapes[1] = sphere2

        val ray = Ray(Point(0, 0, 0.75), Vector.BACKWARD)
        val color = world.colorAt(ray)
        assertEquals(color, world.shapes[1].material.color)
    }

    @Test
    fun `no shadow when nothing obscures light`() {
        val world = World.default()
        val point = Point(0, 0, 10)
        assertFalse(world.isShadowed(point, world.lights.first(), Intersections()))
    }

    @Test
    fun `no shadow when light is behind hit`() {
        val world = World.default()
        val point = Point(-20, 20, -20)
        assertFalse(world.isShadowed(point, world.lights.first(), Intersections()))
    }

    @Test
    fun `no shadow when object is behind hit`() {
        val world = World.default()
        val point = Point(-2, 2, -2)
        assertFalse(world.isShadowed(point, world.lights.first(), Intersections()))
    }

    @Test
    fun `shadow when object is between hit and light`() {
        val world = World.default()
        val point = Point(10, -10, 10)
        assertTrue(world.isShadowed(point, world.lights.first(), Intersections()))
    }

    @Test
    fun `shade hit is given intersection in shadow`() {
        val world = World.default()
        world.lights[0] = Light(Point(0, 0, -10), Color.WHITE)
        world.shapes.add(Sphere())

        val sphere = Sphere()
        sphere.transformation = Transformations.translation(0, 0, 10)
        world.shapes.add(sphere)

        val ray = Ray(Point(0, 0, 5), Vector.FORWARD)
        val intersection = Intersection(4, sphere)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        val color = world.shadeHit(computedHit, Intersections(), 1)
        assertEquals(Color(0.1, 0.1, 0.1), color)
    }

    @Test
    fun `reflected color for a non-reflective material`() {
        val world = World.default()
        val ray = Ray(Point.ORIGIN, Vector.FORWARD)
        val secondShape = world.shapes[1]
        secondShape.material.ambient = 1.0

        val intersection = Intersection(1, secondShape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        val color = world.reflectedColor(computedHit, Intersections(), 1)
        assertEquals(Color.BLACK, color)
    }

    @Test
    fun `reflected color for a reflective material`() {
        val world = World.default()
        val shape = Plane().apply {
            material.reflectiveness = 0.5
            transformation = Transformations.translation(0, -1, 0)
        }
        world.shapes.add(shape)

        val ray = Ray(Point(0, 0, -3), Vector(0, -sqrt(2.0) / 2.0, sqrt(2.0) / 2.0))
        val intersection = Intersection(sqrt(2.0), shape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        val color = world.reflectedColor(computedHit, Intersections(), 1)
        assertEquals(Color(0.19033059782447959, 0.23791324728059948, 0.1427479483683597), color)
    }

    @Test
    fun `shade hit with a reflective material`() {
        val world = World.default()
        val shape = Plane().apply {
            material.reflectiveness = 0.5
            transformation = Transformations.translation(0, -1, 0)
        }
        world.shapes.add(shape)

        val ray = Ray(Point(0, 0, -3), Vector(0, -sqrt(2.0) / 2.0, sqrt(2.0) / 2.0))
        val intersection = Intersection(sqrt(2.0), shape)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        val color = world.shadeHit(computedHit, Intersections(), 1)
        assertEquals(Color(0.8767559865605615, 0.9243386360166814, 0.8291733371044416), color)
    }

    @Test
    fun `no infinite recursion in reflections`() {
        val world = World.default()
        world.shapes = mutableListOf()
        world.lights = mutableListOf(Light(Point.ORIGIN, Color(1, 1, 1)))
        val lower = Plane().apply {
            material.reflectiveness = 1.0
            transformation = Transformations.translation(0, -1, 0)
        }
        val upper = Plane().apply {
            material.reflectiveness = 1.0
            transformation = Transformations.translation(0, 1, 0)
        }
        world.shapes.add(lower)
        world.shapes.add(upper)
        val ray = Ray(Point.ORIGIN, Vector.UP)
        world.colorAt(ray)
    }

    @Test
    fun `reflected color at max recursion depth`() {
        val world = World.default()
        world.shapes = mutableListOf()
        world.lights = mutableListOf(Light(Point.ORIGIN, Color(1, 1, 1)))
        val plane = Plane().apply {
            material.reflectiveness = 0.5
            transformation = Transformations.translation(0, -1, 0)
        }
        world.shapes.add(plane)
        val ray = Ray(Point(0, 0, -3), Vector(0, -sqrt(2.0) / 2.0, sqrt(2.0) / 2.0))
        val intersection = Intersection(sqrt(2.0), plane)
        val computedHit = intersection.prepareComputations(ray, Intersections())
        val color = world.reflectedColor(computedHit, Intersections(), 5)
        assertEquals(Color.BLACK, color)
    }

    @Test
    fun `refracted color with opaque material`() {
        val world = World.default()
        val shape = world.shapes[0]
        val intersections = Intersections(Intersection(4, shape), Intersection(6, shape))
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val computedHit = intersections[0].prepareComputations(ray, intersections)
        val color = world.refractedColor(computedHit, Intersections(), 1)
        assertEquals(Color.BLACK, color)
    }

    @Test
    fun `refracted color at max recursion depth`() {
        val world = World.default()
        val shape = world.shapes[0].apply {
            material.transparency = 1.0
            material.refractiveIndex = 1.5
        }
        val ray = Ray(Point(0, 0, -5), Vector.FORWARD)
        val intersections = Intersections(
            Intersection(4, shape),
            Intersection(6, shape),
        )
        val computedHit = intersections[0].prepareComputations(ray, intersections)
        val color = world.refractedColor(computedHit, Intersections(), 0)
        assertEquals(Color.BLACK, color)
    }

    @Test
    fun `refracted color under total internal reflection`() {
        val world = World.default()
        val shape = world.shapes[0].apply {
            material.transparency = 1.0
            material.refractiveIndex = 1.5
        }
        val ray = Ray(Point(0, 0, sqrt(2.0) / 2.0), Vector.UP)
        val intersections = Intersections(
            Intersection(-sqrt(2.0) / 2.0, shape),
            Intersection(sqrt(2.0) / 2.0, shape),
        )
        val computedHit = intersections[1].prepareComputations(ray, intersections)
        val color = world.refractedColor(computedHit, Intersections(), 5)
        assertEquals(Color.BLACK, color)
    }

    @Test
    fun `refracted color with refracted ray`() {
        val world = World.default()
        val firstShape = world.shapes[0].apply {
            material.ambient = 1.0
            material.pattern = TestPattern()
        }

        val secondShape = world.shapes[1].apply {
            material.transparency = 1.0
            material.refractiveIndex = 1.5
        }
        val ray = Ray(Point(0, 0, 0.1), Vector.UP)
        val intersections = Intersections(
            Intersection(-0.9899, firstShape),
            Intersection(-0.4899, secondShape),
            Intersection(0.4899, secondShape),
            Intersection(0.9899, firstShape),
        )
        val computedHit = intersections[2].prepareComputations(ray, intersections)
        val color = world.refractedColor(computedHit, Intersections(), 5)
        assertEquals(Color(0, 0.9988846746935405, 0.047216423650885055), color)
    }

    @Test
    fun `shade hit with transparent material`() {
        val world = World.default()
        val floor = Plane().apply {
            transformation = Transformations.translation(0, -1, 0)
            material.transparency = 0.5
            material.refractiveIndex = 1.5
        }
        world.shapes.add(floor)
        val ball = Sphere().apply {
            material.color = Color.RED
            material.ambient = 0.5
            transformation = Transformations.translation(0, -3.5, -0.5)
        }
        world.shapes.add(ball)
        val ray = Ray(Point(0, 0, -3), Vector(0, -sqrt(2.0) / 2.0, sqrt(2.0) / 2.0))
        val intersections = Intersections(Intersection(sqrt(2.0), floor))
        val computedHit = intersections[0].prepareComputations(ray, intersections)
        val color = world.shadeHit(computedHit, Intersections(), 5)
        assertEquals(Color(0.9364253887360819, 0.6864253887360819, 0.6864253887360819), color)
    }

    @Test
    fun `shade hit with reflective and transparent material`() {
        val world = World.default()
        val floor = Plane().apply {
            transformation = Transformations.translation(0, -1, 0)
            material.reflectiveness = 0.5
            material.transparency = 0.5
            material.refractiveIndex = 1.5
        }
        world.shapes.add(floor)
        val ball = Sphere().apply {
            material.color = Color.RED
            material.ambient = 0.5
            transformation = Transformations.translation(0, -3.5, -0.5)
        }
        world.shapes.add(ball)
        val ray = Ray(Point(0, 0, -3), Vector(0, -sqrt(2.0) / 2.0, sqrt(2.0) / 2.0))
        val intersections = Intersections(Intersection(sqrt(2.0), floor))
        val computedHit = intersections[0].prepareComputations(ray, intersections)
        val color = world.shadeHit(computedHit, Intersections(), 5)
        assertEquals(Color(0.933915140358792, 0.6964342261158358, 0.6924306911639343), color)
    }
}
