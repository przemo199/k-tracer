package org.example.ktracer.colorproviders.mappings

import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point

/** Tiles the texture across the xz-plane, repeating every unit. */
value object PlanarMapping : Mapping {
    // `mod`, not `%`: negative coordinates must wrap into 0..1 too
    override fun map(point: Point): Coordinates = Coordinates(point.x.mod(1.0), point.z.mod(1.0))
}
