package org.example.ktracer.colorproviders.mappings

import kotlin.test.assertEquals
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CubeFaceTest {
    @ParameterizedTest
    @CsvSource(
        "-1, 0.5, -0.25, LEFT",
        "1.1, -0.75, 0.8, RIGHT",
        "0.1, 0.6, 0.9, FRONT",
        "-0.7, 0, -2, BACK",
        "0.5, 1, 0.9, UP",
        "-0.2, -1.3, 1.1, DOWN",
    )
    fun `identifying the face of a cube from a point`(x: Double, y: Double, z: Double, face: CubeFace) {
        assertEquals(face, CubeFace.of(Point(x, y, z)))
    }

    @ParameterizedTest
    @CsvSource(
        "FRONT, -0.5, 0.5, 1, 0.25, 0.75",
        "FRONT, 0.5, -0.5, 1, 0.75, 0.25",
        "BACK, 0.5, 0.5, -1, 0.25, 0.75",
        "BACK, -0.5, -0.5, -1, 0.75, 0.25",
        "LEFT, -1, 0.5, -0.5, 0.25, 0.75",
        "LEFT, -1, -0.5, 0.5, 0.75, 0.25",
        "RIGHT, 1, 0.5, 0.5, 0.25, 0.75",
        "RIGHT, 1, -0.5, -0.5, 0.75, 0.25",
        "UP, -0.5, 1, -0.5, 0.25, 0.75",
        "UP, 0.5, 1, 0.5, 0.75, 0.25",
        "DOWN, -0.5, -1, 0.5, 0.25, 0.75",
        "DOWN, 0.5, -1, -0.5, 0.75, 0.25",
    )
    fun `UV mapping of each cube face`(face: CubeFace, x: Double, y: Double, z: Double, u: Double, v: Double) {
        assertEquals(Coordinates(u, v), face.coordinates(Point(x, y, z)))
    }
}
