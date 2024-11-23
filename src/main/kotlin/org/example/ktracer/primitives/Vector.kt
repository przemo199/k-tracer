package org.example.ktracer.primitives

import kotlin.math.absoluteValue
import org.example.ktracer.squared
import kotlin.math.sqrt

class Vector(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) : Tuple(x, y, z) {
    override val w get() = 0.0
    val magnitude: Double by lazy(LazyThreadSafetyMode.NONE) { sqrt(x.squared() + y.squared() + z.squared()) }

    constructor(fn: (Int) -> Double) : this(fn(0), fn(1), fn(2))

    fun normalized(): Vector {
        return map { it / magnitude }
    }

    fun reflect(normal: Vector): Vector {
        return this - (normal * 2.0 * (this dot normal))
    }

    operator fun plus(other: Vector): Vector {
        return Vector(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vector): Vector {
        return Vector(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(other: Number): Vector {
        return map(other.toDouble()::times)
    }

    operator fun div(other: Number): Vector {
        return other.toDouble().let {
            map { that -> that / it }
        }
    }

    operator fun unaryMinus(): Vector {
        return map(Double::unaryMinus)
    }

    fun abs(): Vector {
        return map { it.absoluteValue }
    }

    infix fun dot(other: Vector): Double {
        return (x * other.x) + (y * other.y) + (z * other.z)
    }

    infix fun cross(other: Vector): Vector {
        return Vector(
            (y * other.z) - (z * other.y),
            (z * other.x) - (x * other.z),
            (x * other.y) - (y * other.x)
        )
    }

    inline fun map(fn: (Double) -> Double): Vector {
        return Vector(fn(x), fn(y), fn(z))
    }

    fun copy(x: Double = this.x, y: Double = this.y, z: Double = this.z): Vector {
        return Vector(x, y, z)
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
                other is Vector && super.equals(other)
    }

    override fun toString(): String {
        return "Vector(x=$x, y=$y, z=$z)"
    }

    companion object {
        @JvmField
        val ZERO: Vector = Vector()

        @JvmField
        val UP: Vector = Vector(0, 1, 0)

        @JvmField
        val DOWN: Vector = Vector(0, -1, 0)

        @JvmField
        val RIGHT: Vector = Vector(1, 0, 0)

        @JvmField
        val LEFT: Vector = Vector(-1, 0, 0)

        @JvmField
        val FORWARD: Vector = Vector(0, 0, 1)

        @JvmField
        val BACKWARD: Vector = Vector(0, 0, -1)

        operator fun invoke(x: Number, y: Number, z: Number): Vector {
            return Vector(x.toDouble(), y.toDouble(), z.toDouble())
        }
    }
}
