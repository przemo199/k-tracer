package org.example.ktracer.primitives

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MatrixTest {
    @Test
    fun `constructing and inspecting matrix`() {
        val matrix = Matrix(
            listOf(
                1, 2, 3, 4,
                5.5, 6.5, 7.5, 8.5,
                9, 10, 11, 12,
                13.5, 14.5, 15.5, 16.5,
            ),
        )
        assertEquals(1.0, matrix[0, 0])
        assertEquals(4.0, matrix[0, 3])
        assertEquals(5.5, matrix[1, 0])
        assertEquals(7.5, matrix[1, 2])
        assertEquals(11.0, matrix[2, 2])
        assertEquals(13.5, matrix[3, 0])
        assertEquals(15.5, matrix[3, 2])
    }

    @Test
    fun `matrix equality for identical matrices`() {
        val matrix = Matrix(
            listOf(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 8, 7, 6,
                5, 4, 3, 2,
            ),
        )
        val matrix2 = Matrix(
            listOf(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 8, 7, 6,
                5, 4, 3, 2,
            ),
        )
        assertEquals(matrix, matrix2)
    }

    @Test
    fun `matrix equality for different matrices`() {
        val matrix = Matrix(
            listOf(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 8, 7, 6,
                5, 4, 3, 2,
            ),
        )
        val matrix2 = Matrix(
            listOf(
                2, 3, 4, 5,
                6, 7, 8, 9,
                8, 7, 6, 5,
                4, 3, 2, 1,
            ),
        )
        assertNotEquals(matrix, matrix2)
    }

    @Test
    fun `multiplying matrices`() {
        val matrix = Matrix(
            listOf(
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 8, 7, 6,
                5, 4, 3, 2,
            ),
        )
        val matrix2 = Matrix(
            listOf(
                -2, 1, 2, 3,
                3, 2, 1, -1,
                4, 3, 6, 5,
                1, 2, 7, 8,
            ),
        )
        val matrix3 = Matrix(
            listOf(
                20, 22, 50, 48,
                44, 54, 114, 108,
                40, 58, 110, 102,
                16, 26, 46, 42,
            ),
        )
        assertEquals(matrix3, matrix * matrix2)
    }

    @Test
    fun `multiplying matrix by identity matrix`() {
        val matrix = Matrix(
            listOf(
                0, 1, 2, 4,
                1, 2, 4, 8,
                2, 4, 8, 16,
                4, 8, 16, 32,
            ),
        )
        assertEquals(matrix, matrix * Matrix.IDENTITY)
    }

    @Test
    fun `transposing matrix`() {
        val matrix = Matrix(
            listOf(
                0, 9, 3, 0,
                9, 8, 0, 8,
                1, 8, 5, 3,
                0, 0, 5, 8,
            ),
        )
        val matrix2 = Matrix(
            listOf(
                0, 9, 1, 0,
                9, 8, 8, 0,
                3, 0, 5, 5,
                0, 8, 3, 8,
            ),
        )
        assertEquals(matrix2, matrix.transpose())
    }

    @Test
    fun `transposing identity matrix`() {
        assertEquals(Matrix.IDENTITY, Matrix.IDENTITY.transpose())
    }

    @Test
    fun `calculating determinant of matrix`() {
        val matrix = Matrix(
            listOf(
                -2, -8, 3, 5,
                -3, 1, 7, 3,
                1, 2, -9, 6,
                -6, 7, 7, -9,
            ),
        )
        assertEquals(-4071.0, matrix.determinant())
    }

    @Test
    fun `calculating inverse of matrix`() {
        val matrix = Matrix(
            listOf(
                8, -5, 9, 2,
                7, 5, 6, 1,
                -6, 0, 9, 6,
                -3, 0, -9, -4,
            ),
        )
        val matrix2 = Matrix(
            listOf(
                -0.15384615384615385, -0.15384615384615385, -0.28205128205128205, -0.5384615384615384,
                -0.07692307692307693, 0.12307692307692308, 0.02564102564102564, 0.03076923076923077,
                0.358974358974359, 0.358974358974359, 0.4358974358974359, 0.9230769230769231,
                -0.6923076923076923, -0.6923076923076923, -0.7692307692307693, -1.9230769230769231,
            ),
        )
        assertEquals(matrix2, matrix.inverse())
    }

    @Test
    fun `multiply matrix product by its inverse`() {
        val matrix = Matrix(
            listOf(
                3, -9, 7, 3,
                3, -8, 2, -9,
                -4, 4, 4, 1,
                -6, 5, -1, 1,
            ),
        )
        val matrix2 = Matrix(
            listOf(
                8, 2, 2, 2,
                3, -1, 7, 0,
                7, 0, 5, 4,
                6, -2, 0, 5,
            ),
        )
        val matrix3 = matrix * matrix2
        assertEquals(matrix, matrix3 * matrix2.inverse())
    }

    @Test
    fun `negate matrix`() {
        val matrix = Matrix.IDENTITY
        val negatedMatrix = Matrix(
            listOf(
                -1, 0, 0, 0,
                0, -1, 0, 0,
                0, 0, -1, 0,
                0, 0, 0, -1,
            ),
        )
        assertEquals(negatedMatrix, -matrix)
        assertEquals(Matrix.IDENTITY, matrix)
    }
}
