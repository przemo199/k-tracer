package org.example.ktracer.benchmarks

import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Matrix
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

// Single math operations on Vector, Color and Matrix. Each benchmark maps BATCH inputs into a reused output array,
// which JMH consumes when it is returned, so nothing is optimised away and nothing is allocated except the results.

@State(Scope.Thread)
@OperationsPerInvocation(BATCH)
class VectorBenchmark {
    private lateinit var vectors: Array<Vector>
    private lateinit var normals: Array<Vector>
    private lateinit var scalars: DoubleArray
    private val outVectors = arrayOfNulls<Vector>(BATCH)
    private val outDoubles = DoubleArray(BATCH)

    @Setup
    fun setUp() {
        val rng = Rng(1)
        vectors = rng.batch { vector() }
        normals = rng.batch { unitVector() }
        scalars = DoubleArray(BATCH) { rng.range(-2.0, 2.0) }
    }

    @Benchmark
    fun add(): Array<Vector?> {
        for (i in 0 until BATCH) outVectors[i] = vectors[i] + normals[i]
        return outVectors
    }

    @Benchmark
    fun scale(): Array<Vector?> {
        for (i in 0 until BATCH) outVectors[i] = vectors[i] * scalars[i]
        return outVectors
    }

    @Benchmark
    fun dot(): DoubleArray {
        for (i in 0 until BATCH) outDoubles[i] = vectors[i] dot normals[i]
        return outDoubles
    }

    @Benchmark
    fun cross(): Array<Vector?> {
        for (i in 0 until BATCH) outVectors[i] = vectors[i] cross normals[i]
        return outVectors
    }

    @Benchmark
    fun normalized(): Array<Vector?> {
        for (i in 0 until BATCH) outVectors[i] = vectors[i].normalized()
        return outVectors
    }

    @Benchmark
    fun reflect(): Array<Vector?> {
        for (i in 0 until BATCH) outVectors[i] = vectors[i].reflect(normals[i])
        return outVectors
    }
}

@State(Scope.Thread)
@OperationsPerInvocation(BATCH)
class ColorBenchmark {
    private lateinit var colorsA: Array<Color>
    private lateinit var colorsB: Array<Color>
    private lateinit var scalars: DoubleArray
    private val outColors = arrayOfNulls<Color>(BATCH)

    @Setup
    fun setUp() {
        val rng = Rng(2)
        colorsA = rng.batch { color() }
        colorsB = rng.batch { color() }
        scalars = DoubleArray(BATCH) { rng.range(0.0, 1.0) }
    }

    @Benchmark
    fun add(): Array<Color?> {
        for (i in 0 until BATCH) outColors[i] = colorsA[i] + colorsB[i]
        return outColors
    }

    @Benchmark
    fun mul(): Array<Color?> {
        for (i in 0 until BATCH) outColors[i] = colorsA[i] * colorsB[i]
        return outColors
    }

    @Benchmark
    fun scale(): Array<Color?> {
        for (i in 0 until BATCH) outColors[i] = colorsA[i] * scalars[i]
        return outColors
    }
}

@State(Scope.Thread)
@OperationsPerInvocation(BATCH)
class MatrixBenchmark {
    private lateinit var matricesA: Array<Matrix>
    private lateinit var matricesB: Array<Matrix>
    private lateinit var points: Array<Point>
    private lateinit var vectors: Array<Vector>
    private lateinit var viewFrom: Array<Point>
    private lateinit var viewTo: Array<Point>
    private val outPoints = arrayOfNulls<Point>(BATCH)
    private val outVectors = arrayOfNulls<Vector>(BATCH)
    private val outMatrices = arrayOfNulls<Matrix>(BATCH)

    @Setup
    fun setUp() {
        val rng = Rng(3)
        matricesA = rng.batch { transformation() }
        matricesB = rng.batch { transformation() }
        points = rng.batch { point() }
        vectors = rng.batch { vector() }
        viewFrom = rng.batch { (vector() * 5.0).toPoint() }
        viewTo = rng.batch { pointInBall(Point.ORIGIN, 1.0) }
    }

    @Benchmark
    fun mulPoint(): Array<Point?> {
        for (i in 0 until BATCH) outPoints[i] = matricesA[i] * points[i]
        return outPoints
    }

    @Benchmark
    fun mulVector(): Array<Vector?> {
        for (i in 0 until BATCH) outVectors[i] = matricesA[i] * vectors[i]
        return outVectors
    }

    @Benchmark
    fun mulMatrix(): Array<Matrix?> {
        for (i in 0 until BATCH) outMatrices[i] = matricesA[i] * matricesB[i]
        return outMatrices
    }

    @Benchmark
    fun transpose(): Array<Matrix?> {
        for (i in 0 until BATCH) outMatrices[i] = matricesA[i].transpose()
        return outMatrices
    }

    /** The real computation, not the per-instance cache: see [UncachedMatrices]. */
    @Benchmark
    fun inverse(uncached: UncachedMatrices): Array<Matrix?> {
        val matrices = uncached.matrices
        for (i in 0 until BATCH) outMatrices[i] = matrices[i]!!.inverse()
        return outMatrices
    }

    @Benchmark
    fun viewTransform(): Array<Matrix?> {
        for (i in 0 until BATCH) outMatrices[i] = Transformations.viewTransform(viewFrom[i], viewTo[i], Vector.UP)
        return outMatrices
    }
}

/**
 * `Matrix.inverse()` caches its result on the instance, so after the first invocation [MatrixBenchmark.inverse] would
 * only time the cache lookup. Before every invocation (untimed) this hands out new instances: `copy()` shares the
 * element array but not the cache.
 */
@State(Scope.Thread)
class UncachedMatrices {
    private lateinit var sources: Array<Matrix>
    val matrices = arrayOfNulls<Matrix>(BATCH)

    @Setup(Level.Trial)
    fun create() {
        sources = Rng(4).batch { transformation() }
    }

    @Setup(Level.Invocation)
    fun refresh() {
        for (i in 0 until BATCH) matrices[i] = sources[i].copy()
    }
}
