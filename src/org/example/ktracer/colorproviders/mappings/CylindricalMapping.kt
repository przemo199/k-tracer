package org.example.ktracer.colorproviders.mappings

import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Point

/**
 * Wraps the texture around a unit cylinder like a label on a can: `u` goes around the y-axis, `v` up the side,
 * repeating every unit of height (`v = 0` at `y = 0`, `y = 1`, …).
 */
value object CylindricalMapping : Mapping {
    override fun map(point: Point): Coordinates = Coordinates(aroundYAxis(point), point.y.mod(1.0))
}
