package org.example.ktracer.colorproviders.mappings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.ktracer.coarseEquals
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

// √2 / 2, written out because annotation arguments must be constants
private const val HALF_SQRT_2 = 0.7071067811865476

private fun assertCoordinates(u: Double, v: Double, actual: Coordinates) {
    assertTrue(actual.u coarseEquals u && actual.v coarseEquals v, "expected ($u, $v), got (${actual.u}, ${actual.v})")
}

class MappingTest {
    @ParameterizedTest
    @CsvSource(
        "0.25, 0, 0.5, 0.25, 0.5",
        "0.25, 0, -0.25, 0.25, 0.75",
        "0.25, 0.5, -0.25, 0.25, 0.75",
        "1.25, 0, 0.5, 0.25, 0.5",
        "0.25, 0, -1.75, 0.25, 0.25",
        "1, 0, -1, 0.0, 0.0",
        "0, 0, 0, 0.0, 0.0",
    )
    fun `planar mapping on a 3D point`(x: Double, y: Double, z: Double, u: Double, v: Double) {
        assertCoordinates(u, v, PlanarMapping.map(Point(x, y, z)))
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, -1, 0.0, 0.5",
        "1, 0, 0, 0.25, 0.5",
        "0, 0, 1, 0.5, 0.5",
        "-1, 0, 0, 0.75, 0.5",
        "0, 1, 0, 0.5, 1.0",
        "0, -1, 0, 0.5, 0.0",
        "$HALF_SQRT_2, $HALF_SQRT_2, 0, 0.25, 0.75",
    )
    fun `spherical mapping on a 3D point`(x: Double, y: Double, z: Double, u: Double, v: Double) {
        assertCoordinates(u, v, SphericalMapping.map(Point(x, y, z)))
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0, -1, 0.0, 0.0",
        "0, 0.5, -1, 0.0, 0.5",
        "0, 1, -1, 0.0, 0.0",
        "$HALF_SQRT_2, 0.5, -$HALF_SQRT_2, 0.125, 0.5",
        "1, 0.5, 0, 0.25, 0.5",
        "$HALF_SQRT_2, 0.5, $HALF_SQRT_2, 0.375, 0.5",
        "0, -0.25, 1, 0.5, 0.75",
        "-$HALF_SQRT_2, 0.5, $HALF_SQRT_2, 0.625, 0.5",
        "-1, 1.25, 0, 0.75, 0.25",
        "-$HALF_SQRT_2, 0.5, -$HALF_SQRT_2, 0.875, 0.5",
    )
    fun `cylindrical mapping on a 3D point`(x: Double, y: Double, z: Double, u: Double, v: Double) {
        assertCoordinates(u, v, CylindricalMapping.map(Point(x, y, z)))
    }

    @Test
    fun `cylindrical and spherical mappings agree on u`() {
        for (point in listOf(Point(0.3, 0.2, -0.9), Point(-0.8, -0.4, 0.4))) {
            assertEquals(CylindricalMapping.map(point).u, SphericalMapping.map(point).u)
        }
    }
}
