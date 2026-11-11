package org.example.ktracer.benchmarks

import kotlin.math.max
import org.example.ktracer.composites.Intersection
import org.example.ktracer.composites.Intersections
import org.example.ktracer.composites.Ray
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector
import org.example.ktracer.shapes.Cone
import org.example.ktracer.shapes.Cube
import org.example.ktracer.shapes.Cylinder
import org.example.ktracer.shapes.Plane
import org.example.ktracer.shapes.Shape
import org.example.ktracer.shapes.Sphere
import org.example.ktracer.shapes.Triangle
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole

// Ray-shape intersection, surface normals and hit selection.

/** A shape and a sampler of points inside it. All but the plane fit within radius 1.8. */
enum class ShapeCase(val create: () -> Shape, val inside: Rng.() -> Point) {
    SPHERE({ Sphere() }, { pointInBall(Point.ORIGIN, 0.5) }),
    PLANE({ Plane() }, { Point(range(-2.0, 2.0), 0.0, range(-2.0, 2.0)) }),
    CUBE({ Cube() }, { pointInBall(Point.ORIGIN, 0.5) }),
    CYLINDER({ Cylinder(-1, 1, closed = true) }, { pointInBall(Point.ORIGIN, 0.5) }),
    CONE({ Cone(-1, 0, closed = true) }, { pointInBall(Point(0.0, -0.6, 0.0), 0.3) }),
    TRIANGLE(
        { Triangle(Point(0.0, 1.0, 0.0), Point(-1.0, 0.0, 0.0), Point(1.0, 0.0, 0.0)) },
        { Point(range(-0.4, 0.4), range(0.05, 0.5), 0.0) },
    );

    fun hitRays(rng: Rng, size: Int = BATCH): Array<Ray> = rng.rays(size) { inside() }

    companion {
        fun of(name: String): ShapeCase = valueOf(name.uppercase())
    }
}

/** Rays from 5 units away from the origin through `target`. */
fun Rng.rays(size: Int = BATCH, target: Rng.(outward: Vector) -> Point): Array<Ray> = batch(size) {
    val outward = unitVector()
    val origin = Point.ORIGIN + outward * 5.0
    Ray(origin, (target(outward) - origin).normalized())
}

/** Rays through a point 3 units off-center: they pass at least 2.5 units from the origin, missing every bounded shape. */
fun Rng.missRays(size: Int = BATCH): Array<Ray> = rays(size) { outward ->
    val random = unitVector()
    val perpendicular = (random - outward * (random dot outward)).normalized()
    Point.ORIGIN + perpendicular * 3.0
}

/**
 * Sends every shape type through the call sites a render uses (`Ray.intersect` -> `Shape.localIntersect`,
 * `Shape.normalAt` -> `Shape.localNormalAt`) before measuring. Each JMH fork otherwise sees a single shape type,
 * and the JIT inlines it, which a real scene with mixed shapes never allows.
 */
fun polluteShapeCallSites() {
    val rng = Rng(99)
    val intersections = Intersections()
    val cases = ShapeCase.entries.map { it.create() to it.hitRays(rng, 256) }
    repeat(20) {
        for ([shape, rays] in cases) {
            for (ray in rays) {
                intersections.clear()
                ray.intersect(shape, intersections)
                intersections.hit()?.let { shape.normalAt(ray.position(it.distance)) }
            }
        }
    }
}

@State(Scope.Thread)
@OperationsPerInvocation(BATCH)
class IntersectBenchmark {
    /** `<shape>/<hit|miss>`; an infinite plane is hit by every non-parallel ray, so it has no miss case. */
    @Param(
        "sphere/hit", "sphere/miss", "plane/hit", "cube/hit", "cube/miss", "cylinder/hit", "cylinder/miss",
        "cone/hit", "cone/miss", "triangle/hit", "triangle/miss",
    )
    lateinit var case: String

    private lateinit var shape: Shape
    private lateinit var rays: Array<Ray>
    private val intersections = Intersections()

    @Setup
    fun setUp() {
        polluteShapeCallSites()
        val shapeCase = ShapeCase.of(case.substringBefore('/'))
        val expectHit = case.substringAfter('/') == "hit"
        val rng = Rng(10)
        shape = shapeCase.create()
        rays = if (expectHit) shapeCase.hitRays(rng) else rng.missRays()
        // make sure the benchmark measures what its name says
        for (ray in rays) {
            intersections.clear()
            ray.intersect(shape, intersections)
            check((intersections.hit() != null) == expectHit) { "$case: $ray" }
        }
    }

    @Benchmark
    fun intersect(blackhole: Blackhole) {
        for (ray in rays) {
            intersections.clear()
            ray.intersect(shape, intersections)
            blackhole.consume(intersections)
        }
    }
}

@State(Scope.Thread)
@OperationsPerInvocation(BATCH)
class NormalAtBenchmark {
    @Param("sphere", "cube")
    lateinit var shape: String

    private lateinit var target: Shape
    private lateinit var points: Array<Point>
    private val normals = arrayOfNulls<Vector>(BATCH)

    @Setup
    fun setUp() {
        polluteShapeCallSites()
        val rng = Rng(11)
        target = ShapeCase.of(shape).create()
        points = when (shape) {
            "sphere" -> rng.batch { unitVector().toPoint() }
            // project onto the cube's surface along the largest component
            else -> rng.batch {
                val vector = unitVector()
                val abs = vector.abs()
                (vector / max(abs.x, max(abs.y, abs.z))).toPoint()
            }
        }
    }

    @Benchmark
    fun normalAt(): Array<Vector?> {
        for (i in 0 until BATCH) normals[i] = target.normalAt(points[i])
        return normals
    }
}

const val INTERSECTIONS_PER_LIST = 8

const val LISTS = BATCH / INTERSECTIONS_PER_LIST

/** Scores are per list of [INTERSECTIONS_PER_LIST] intersections. */
@State(Scope.Thread)
@OperationsPerInvocation(LISTS)
class IntersectionsBenchmark {
    private lateinit var lists: Array<Intersections>
    private val hits = arrayOfNulls<Intersection>(LISTS)

    @Setup
    fun setUp() {
        val rng = Rng(12)
        val sphere = Sphere()
        // some intersections behind the ray origin (negative), as shadows and recursion produce
        lists = Array(LISTS) {
            Intersections().apply {
                repeat(INTERSECTIONS_PER_LIST) { add(Intersection(rng.range(-5.0, 10.0), sphere)) }
            }
        }
    }

    @Benchmark
    fun hit(): Array<Intersection?> {
        for (i in 0 until LISTS) hits[i] = lists[i].hit()
        return hits
    }
}
