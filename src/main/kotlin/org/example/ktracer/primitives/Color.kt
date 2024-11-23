package org.example.ktracer.primitives

class Color(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0) : Tuple(red, green, blue) {
    val red by this::x
    val green by this::y
    val blue by this::z
    override val w get() = 1.0

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

    operator fun times(other: Number): Color {
        return map(other.toDouble()::times)
    }

    operator fun div(other: Number): Color {
        return other.toDouble().let { that ->
            map { it / that }
        }
    }

    inline fun map(fn: (Double) -> Double): Color {
        return Color(fn(red), fn(green), fn(blue))
    }

    fun copy(red: Double = this.red, green: Double = this.green, blue: Double = this.blue): Color {
        return Color(red, green, blue)
    }

    fun clamped(): Color {
        return map(::clamp)
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is Color && super.equals(other)
    }

    override fun toString(): String {
        return "Color(red=$x, green=$y, blue=$z)"
    }

    companion object {
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

        fun clamp(value: Double): Double {
            return value.coerceIn(MIN_VALUE, MAX_VALUE)
        }

        operator fun invoke(x: Number, y: Number, z: Number): Color {
            return Color(x.toDouble(), y.toDouble(), z.toDouble())
        }
    }
}
