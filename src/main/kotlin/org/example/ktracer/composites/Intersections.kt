package org.example.ktracer.composites

import org.example.ktracer.MAX

class Intersections(vararg intersection: Intersection) : ArrayList<Intersection>(intersection.toList()) {
    fun hit(): Intersection? {
        var maybeHit: Intersection? = null
        var hitDistance = MAX
        for (intersection in this) {
            if (intersection.isWithinDistance(hitDistance)) {
                maybeHit = intersection
                hitDistance = intersection.distance
            }
        }
        return maybeHit
    }

    fun sortByDistance() {
        listOf(1).size
        sortBy { it.distance }
    }
}
