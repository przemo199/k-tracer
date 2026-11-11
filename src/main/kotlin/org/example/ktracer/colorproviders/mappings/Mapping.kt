package org.example.ktracer.colorproviders.mappings

import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point

interface Mapping {
    fun map(point: Point): Coordinates
}
