package org.example.ktracer.primitives

import kotlin.math.absoluteValue

class Point(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) : Tuple(x, y, z) {
    operator fun plus(other: Vector): Point {
        return Point(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Point): Vector {
        return Vector(x - other.x, y - other.y, z - other.z)
    }

    operator fun minus(other: Vector): Point {
        return Point(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(other: Number): Point {
        return map(other.toDouble()::times)
    }

    operator fun div(other: Number): Point {
        return other.toDouble().let {
            map { that -> that / it}
        }
    }

    operator fun unaryMinus(): Point {
        return map(Double::unaryMinus)
    }

    fun abs(): Point {
        return map { it.absoluteValue }
    }

    inline fun map(fn: (Double) -> Double): Point {
        return Point(fn(x), fn(y), fn(z))
    }

    fun copy(x: Double = this.x, y: Double = this.y, z: Double = this.z): Point {
        return Point(x, y, z)
    }

    override fun component4() = 1.0

    override fun equals(other: Any?): Boolean {
        return this === other || other is Point && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Point(x=$x, y=$y, z=$z)"
    }

    companion object {
        @JvmField
        val ORIGIN: Point = Point()

        operator fun invoke(x: Number, y: Number, z: Number): Point {
            return Point(x.toDouble(), y.toDouble(), z.toDouble())
        }
    }
}
