package org.example.ktracer.colorproviders.mappings

import kotlin.math.PI
import kotlin.math.atan2
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point

object CylindricalMapping : Mapping {
    override fun map(point: Point): Coordinates {
        val theta = atan2(point.x, point.z)
        val rawU = theta / (2 * PI)
        val u = 1 - (rawU + 0.5)
        val v = point.y % 1
        return Coordinates(u, v)
    }
}