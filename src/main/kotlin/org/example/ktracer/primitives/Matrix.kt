package org.example.ktracer.primitives

import org.example.ktracer.coarseEquals

typealias Transformation = Matrix

/**
 * Square matrix
 */
data class Matrix(val elements: DoubleArray) {
    private var inverseCache: Matrix? = null

    constructor(elements: Iterable<Number>) : this(elements.map(Number::toDouble).toDoubleArray())

    fun transpose(): Matrix {
        val result = Matrix(elements.copyOf())
        for (row in 0..<SIDE_LENGTH) {
            for (column in row..<SIDE_LENGTH) {
                result[row, column] = result[column, row].also {
                    result[column, row] = result[row, column]
                }
            }
        }
        return result
    }

    fun determinant(): Double {
        val a = elements[0]
        val b = elements[1]
        val c = elements[2]
        val d = elements[3]
        val e = elements[4]
        val f = elements[5]
        val g = elements[6]
        val h = elements[7]
        val i = elements[8]
        val j = elements[9]
        val k = elements[10]
        val l = elements[11]
        val m = elements[12]
        val n = elements[13]
        val o = elements[14]
        val p = elements[15]

        return (a * ((f * ((k * p) - (l * o))) - (g * ((j * p) - (l * n))) + (h * ((j * o) - (k * n))))) -
                (b * ((e * ((k * p) - (l * o))) - (g * ((i * p) - (l * m))) + (h * ((i * o) - (k * m))))) +
                (c * ((e * ((j * p) - (l * n))) - (f * ((i * p) - (l * m))) + (h * ((i * n) - (j * m))))) -
                (d * ((e * ((j * o) - (k * n))) - (f * ((i * o) - (k * m))) + (g * ((i * n) - (j * m)))))
    }

    fun invertible(): Boolean {
        return determinant() != 0.0
    }

    fun isIdentity(): Boolean {
        for (row in 0..SIDE_LENGTH) {
            for (column in 0..SIDE_LENGTH) {
                if ((row == column && !(get(row, column) coarseEquals 1.0)) ||
                    !(get(row, column) coarseEquals 0.0)) {
                    return false
                }
            }
        }

        return true
    }

    fun inverse(): Matrix {
        inverseCache?.let {
            return it
        }

        if (isIdentity()) {
            inverseCache = this
            return this
        }

        val a00 = elements[0]
        val a01 = elements[1]
        val a02 = elements[2]
        val a03 = elements[3]
        val a10 = elements[4]
        val a11 = elements[5]
        val a12 = elements[6]
        val a13 = elements[7]
        val a20 = elements[8]
        val a21 = elements[9]
        val a22 = elements[10]
        val a23 = elements[11]
        val a30 = elements[12]
        val a31 = elements[13]
        val a32 = elements[14]
        val a33 = elements[15]

        val b00 = a00 * a11 - a01 * a10
        val b01 = a00 * a12 - a02 * a10
        val b02 = a00 * a13 - a03 * a10
        val b03 = a01 * a12 - a02 * a11
        val b04 = a01 * a13 - a03 * a11
        val b05 = a02 * a13 - a03 * a12
        val b06 = a20 * a31 - a21 * a30
        val b07 = a20 * a32 - a22 * a30
        val b08 = a20 * a33 - a23 * a30
        val b09 = a21 * a32 - a22 * a31
        val b10 = a21 * a33 - a23 * a31
        val b11 = a22 * a33 - a23 * a32

        val determinant = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06
        require(determinant != 0.0) { "Matrix is not invertible" }

        val inverse = Matrix(
            doubleArrayOf(
                (a11 * b11 - a12 * b10 + a13 * b09) / determinant,
                (a02 * b10 - a01 * b11 - a03 * b09) / determinant,
                (a31 * b05 - a32 * b04 + a33 * b03) / determinant,
                (a22 * b04 - a21 * b05 - a23 * b03) / determinant,
                (a12 * b08 - a10 * b11 - a13 * b07) / determinant,
                (a00 * b11 - a02 * b08 + a03 * b07) / determinant,
                (a32 * b02 - a30 * b05 - a33 * b01) / determinant,
                (a20 * b05 - a22 * b02 + a23 * b01) / determinant,
                (a10 * b10 - a11 * b08 + a13 * b06) / determinant,
                (a01 * b08 - a00 * b10 - a03 * b06) / determinant,
                (a30 * b04 - a31 * b02 + a33 * b00) / determinant,
                (a21 * b02 - a20 * b04 - a23 * b00) / determinant,
                (a11 * b07 - a10 * b09 - a12 * b06) / determinant,
                (a00 * b09 - a01 * b07 + a02 * b06) / determinant,
                (a31 * b01 - a30 * b03 - a32 * b00) / determinant,
                (a20 * b03 - a21 * b01 + a22 * b00) / determinant,
            )
        )
        inverseCache = inverse
        return inverse
    }

    operator fun get(x: Int, y: Int): Double {
        return elements[rowColToIndex(x, y)]
    }

    operator fun set(x: Int, y: Int, value: Double) {
        elements[rowColToIndex(x, y)] = value
    }

    operator fun set(x: Int, y: Int, value: Number) {
        elements[rowColToIndex(x, y)] = value.toDouble()
    }

    operator fun times(other: Matrix): Matrix {
        val result = EMPTY
        for (row in 0..<SIDE_LENGTH) {
            for (column in 0..<SIDE_LENGTH) {
                var sum = 0.0
                for (index in 0..<SIDE_LENGTH) {
                    sum += this[row, index] * other[index, column]
                }
                result[row, column] = sum
            }
        }
        return result
    }

    operator fun times(other: Point): Point {
        val result = DoubleArray(SIDE_LENGTH) { 0.0 }
        for (row in 0..<SIDE_LENGTH) {
            var sum = 0.0
            for (col in 0..<SIDE_LENGTH) {
                sum += this[row, col] * other[col]
            }
            result[row] = sum
        }
        return Point(result[0], result[1], result[2])
    }

    operator fun times(other: Vector): Vector {
        val result = DoubleArray(SIDE_LENGTH) { 0.0 }
        for (row in 0..<SIDE_LENGTH) {
            var sum = 0.0
            for (col in 0..<SIDE_LENGTH) {
                sum += this[row, col] * other[col]
            }
            result[row] = sum
        }
        return Vector(result[0], result[1], result[2])
    }

    override fun equals(other: Any?): Boolean {
        return this === other ||
            other is Matrix &&
            elements.zip(other.elements).all { it.first coarseEquals it.second }
    }

    override fun hashCode(): Int {
        return elements.contentHashCode()
    }

    override fun toString(): String {
        return "Matrix(elements=${arrayOf(elements).contentDeepToString()})"
    }

    companion object {
        const val SIDE_LENGTH = 4

        const val INDICES = SIDE_LENGTH * SIDE_LENGTH

        /**
         * Empty matrix
         */
        @JvmStatic
        val EMPTY get() = Matrix(DoubleArray(INDICES) { 0.0 })

        /**
         * Identity matrix
         */
        @JvmStatic
        val IDENTITY get() = Matrix()

        @JvmStatic
        private fun rowColToIndex(x: Int, y: Int): Int {
            return (x * SIDE_LENGTH) + y
        }

        /**
         * Creates identity matrix
         */
        operator fun invoke(): Matrix {
            val matrix = EMPTY
            for (index in 0..<SIDE_LENGTH) {
                matrix[index, index] = 1.0
            }
            return matrix
        }
    }
}
