package org.example.ktracer.primitives

import kotlin.math.absoluteValue

class Point(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) : Tuple(x, y, z) {
    override val w get() = 1.0

    constructor(fn: (Int) -> Double) : this(fn(0), fn(1), fn(2))

    operator fun plus(other: Vector): Point = Point(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Point): Vector = Vector(x - other.x, y - other.y, z - other.z)

    operator fun minus(other: Vector): Point = Point(x - other.x, y - other.y, z - other.z)

    operator fun times(other: Number): Point = map(other.toDouble()::times)

    operator fun div(other: Number): Point = other.toDouble().let { map { that -> that / it } }

    operator fun unaryMinus(): Point = map(Double::unaryMinus)

    fun abs(): Point = map(Double::absoluteValue)

    inline fun map(fn: (Double) -> Double): Point = Point(fn(x), fn(y), fn(z))

    fun copy(x: Double = this.x, y: Double = this.y, z: Double = this.z): Point = Point(x, y, z)

    override fun equals(other: Any?): Boolean = this === other || other is Point && super.equals(other)

    override fun toString(): String = "Point(x=$x, y=$y, z=$z)"

    companion object {
        @JvmField
        val ORIGIN: Point = Point()

        operator fun invoke(x: Number, y: Number, z: Number): Point = Point(x.toDouble(), y.toDouble(), z.toDouble())
    }
}
