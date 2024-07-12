package org.example.ktracer.primitives

class Color(red: Double = 0.0, green: Double = 0.0, blue: Double = 0.0) : Tuple(red, green, blue) {
    val red by this::x
    val green by this::y
    val blue by this::z

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
        return other.toDouble().let {
            map { that -> that / it}
        }
    }

    inline fun map(fn: (Double) -> Double): Color {
        return Color(fn(red), fn(green), fn(blue))
    }

    fun copy(red: Double = this.red, green: Double = this.green, blue: Double = this.blue): Color {
        return Color(red, green, blue)
    }

    fun clamp(): Color {
        return map(::clamp)
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is Color && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Color(red=$x, green=$y, blue=$z)"
    }

    companion object {
        const val MIN_COLOR_VALUE = 0.0

        const val MAX_COLOR_VALUE = 1.0

        @JvmField
        val BLACK = Color(MIN_COLOR_VALUE, MIN_COLOR_VALUE, MIN_COLOR_VALUE)

        @JvmField
        val WHITE = Color(MAX_COLOR_VALUE, MAX_COLOR_VALUE, MAX_COLOR_VALUE)

        @JvmField
        val RED = Color(MAX_COLOR_VALUE, MIN_COLOR_VALUE, MIN_COLOR_VALUE)

        @JvmField
        val GREEN = Color(MIN_COLOR_VALUE, MAX_COLOR_VALUE, MIN_COLOR_VALUE)

        @JvmField
        val BLUE = Color(MIN_COLOR_VALUE, MIN_COLOR_VALUE, MAX_COLOR_VALUE)

        fun clamp(value: Double): Double {
            return value.coerceIn(MIN_COLOR_VALUE, MAX_COLOR_VALUE)
        }

        operator fun invoke(x: Number, y: Number, z: Number): Color {
            return Color(x.toDouble(), y.toDouble(), z.toDouble())
        }
    }
}
