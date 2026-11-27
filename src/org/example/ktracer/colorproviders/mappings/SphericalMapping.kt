package org.example.ktracer.colorproviders.mappings

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point
import org.example.ktracer.squared

/**
 * Wraps the texture around a unit sphere like latitude and longitude: `u` goes around the y-axis, `v` from the
 * south pole (0) to the north pole (1).
 */
value object SphericalMapping : Mapping {
    override fun map(point: Point): Coordinates {
        val radius = sqrt(point.x.squared() + point.y.squared() + point.z.squared())
        // polar angle from the north pole, in 0..π
        val phi = acos(point.y / radius)
        return Coordinates(aroundYAxis(point), 1 - (phi / PI))
    }
}
