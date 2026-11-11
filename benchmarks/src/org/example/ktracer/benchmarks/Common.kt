package org.example.ktracer.benchmarks

import kotlin.math.PI
import kotlin.math.cbrt
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformation
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.primitives.Vector

/** Inputs per benchmark invocation; a single operation (~1 ns) is too fast to time on its own. Scores are per input. */
const val BATCH = 1024

private val GOLDEN_GAMMA = 0x9E3779B97F4A7C15uL.toLong()
private val MIX_1 = 0xBF58476D1CE4E5B9uL.toLong()
private val MIX_2 = 0x94D049BB133111EBuL.toLong()

/** SplitMix64 PRNG with a fixed seed, so every run measures exactly the same inputs. */
class Rng(seed: Long) {
    private var state = seed

    /** Uniform value in `[min, max)`. */
    fun range(min: Double, max: Double): Double {
        state += GOLDEN_GAMMA
        var z = state
        z = (z xor (z ushr 30)) * MIX_1
        z = (z xor (z ushr 27)) * MIX_2
        val unit = ((z xor (z ushr 31)) ushr 11).toDouble() / (1L shl 53).toDouble()
        return Math.fma(max - min, unit, min)
    }

    fun vector(): Vector = Vector(range(-1.0, 1.0), range(-1.0, 1.0), range(-1.0, 1.0))

    fun unitVector(): Vector {
        while (true) {
            val vector = vector()
            val magnitude = vector.magnitude
            if (magnitude > 0.01 && magnitude <= 1.0) {
                return vector / magnitude
            }
        }
    }

    fun point(): Point = vector().toPoint()

    fun pointInBall(center: Point, radius: Double): Point = center + unitVector() * (radius * cbrt(range(0.0, 1.0)))

    fun color(): Color = Color(range(0.0, 1.0), range(0.0, 1.0), range(0.0, 1.0))

    /** Random invertible transformation. */
    fun transformation(): Transformation {
        val offset = vector() * 5.0
        return Transformations.translation(offset.x, offset.y, offset.z) *
                Transformations.rotationX(range(0.0, 2.0 * PI)) *
                Transformations.rotationY(range(0.0, 2.0 * PI)) *
                Transformations.scaling(range(0.5, 2.0), range(0.5, 2.0), 1.5)
    }

    inline fun <reified T> batch(size: Int = BATCH, generate: Rng.() -> T): Array<T> = Array(size) { generate() }
}

fun Vector.toPoint(): Point = Point(x, y, z)
