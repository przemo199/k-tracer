package org.example.ktracer.colorproviders.mappings

import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point

object PlanarMapping : Mapping {
    override fun map(point: Point): Coordinates = Coordinates(point.x % 1.0, point.z % 1.0)
}
