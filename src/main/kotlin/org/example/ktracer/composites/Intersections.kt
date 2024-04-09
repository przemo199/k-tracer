package org.example.ktracer.composites

import org.example.ktracer.MAX

class Intersections(vararg intersection: Intersection) : ArrayList<Intersection>(intersection.toList()) {
    fun hit(): Intersection? {
        var maybeHit: Intersection? = null
        var hitDistance = MAX
        this.forEach {
            if (it.isWithinDistance(hitDistance)) {
                maybeHit = it
                hitDistance = it.distance
            }
        }
        return maybeHit
    }

    fun sortByDistance() {
        sortBy { it.distance }
    }
}
