package org.example.ktracer.primitives

import org.example.ktracer.coarseEquals

value class Color(val red: Double = 0.0, val green: Double = 0.0, val blue: Double = 0.0) {
//    val red by this::x
//    val green by this::y
//    val blue by this::z

    constructor(fn: (Int) -> Double) : this(fn(0), fn(1), fn(2))

    operator fun plus(other: Color): Color {
        return Color(red + other.red, green + other.green, blue + other.blue)
    }

    operator fun minus(other: Color): Color {
        return Color(red - other.red, green - other.green, blue - other.blue)
    }

    operator fun times(other: Color): Color {
        return Color(red * other.red, green * other.green, blue * other.blue)
    }

    operator fun times(other: Number): Color = map(other.toDouble()::times)

    operator fun div(other: Number): Color = other.toDouble().let { map { that -> that / it } }

    inline fun map(fn: (Double) -> Double): Color = Color(fn(red), fn(green), fn(blue))

    fun copy(red: Double = this.red, green: Double = this.green, blue: Double = this.blue): Color {
        return Color(red, green, blue)
    }

    fun clamped(): Color = map(::clamp)

    override fun equals(other: Any?): Boolean = other is Color
            && red coarseEquals other.red
            && green coarseEquals other.green
            && blue coarseEquals other.blue

    override fun toString(): String = "Color(red=$red, green=$green, blue=$blue)"

    companion {
        const val MIN_VALUE = 0.0

        const val MAX_VALUE = 1.0

        @JvmField
        val BLACK = Color(MIN_VALUE, MIN_VALUE, MIN_VALUE)

        @JvmField
        val WHITE = Color(MAX_VALUE, MAX_VALUE, MAX_VALUE)

        @JvmField
        val RED = Color(MAX_VALUE, MIN_VALUE, MIN_VALUE)

        @JvmField
        val GREEN = Color(MIN_VALUE, MAX_VALUE, MIN_VALUE)

        @JvmField
        val BLUE = Color(MIN_VALUE, MIN_VALUE, MAX_VALUE)

        fun clamp(value: Double) = value.coerceIn(MIN_VALUE, MAX_VALUE)

        operator fun invoke(x: Number, y: Number, z: Number): Color = Color(x.toDouble(), y.toDouble(), z.toDouble())
    }
}
