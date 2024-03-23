package org.example.ktracer.primitives

class Point(x: Number = 0, y: Number = 0, z: Number = 0) : Tuple(x, y, z) {
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
        other.toDouble().also {
            return Point(x * it, y * it, z * it)
        }
    }

    operator fun div(other: Number): Point {
        other.toDouble().also {
            return Point(x / it, y / it, z / it)
        }
    }

    operator fun unaryMinus(): Point {
        return Point(-x, -y, -z)
    }

    fun copy(x: Double = this.x, y: Double = this.y, z: Double = this.z): Point {
        return Point(x, y, z)
    }

    override fun equals(other: Any?): Boolean {
        return other is Point && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Point(x=$x, y=$y, z=$z)"
    }

    companion object {
        @JvmStatic
        val ORIGIN: Point = Point()
    }
}
