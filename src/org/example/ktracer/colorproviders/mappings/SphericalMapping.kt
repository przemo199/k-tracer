package org.example.ktracer.colorproviders.mappings

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Vector

object SphericalMapping : Mapping {
    override fun map(point: Point): Coordinates {
        val theta = atan2(point.x, point.z)
        val radius = Vector(point.x, point.y, point.z).magnitude
        val phi = acos(point.y / radius)
        val rawU = theta / (2 * PI)
        val u = 1 - (rawU + 0.5)
        val v = 1 - (phi / PI)
        return Coordinates(u, v)
    }
}