package org.example.ktracer.composites

class Intersections : MutableList<Intersection> by ArrayList() {
    fun hit(): Intersection? {
        return this.filter { it.distance >= 0.0 }.minOrNull()
    }

    companion object {
        operator fun invoke(vararg intersections: Intersection): Intersections {
            return Intersections().apply {
                addAll(intersections)
            }
        }
    }
}
