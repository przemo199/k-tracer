package org.example.ktracer.primitives

class Color(red: Number = 0, green: Number = 0, blue: Number = 0) : Tuple(red, green, blue) {
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
        other.toDouble().also {
            return Color(red * it, green * it, blue * it)
        }
    }

    fun copy(red: Double = this.red, green: Double = this.green, blue: Double = this.blue): Color {
        return Color(red, green, blue)
    }

    override fun equals(other: Any?): Boolean {
        return other is Color && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Color(red=$x, green=$y, blue=$z)"
    }

    companion object {
        val BLACK = Color(0.0, 0.0, 0.0)

        val WHITE = Color(1.0, 1.0, 1.0)

        val RED = Color(1.0, 0.0, 0.0)

        val GREEN = Color(0.0, 1.0, 0.0)

        val BLUE = Color(0.0, 0.0, 1.0)
    }
}
