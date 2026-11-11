package org.example.ktracer.composites

class Intersections : MutableList<Intersection> by ArrayList() {
    fun hit(): Intersection? = filter { it.distance >= 0.0 }.minOrNull()

    companion {
        operator fun invoke(vararg intersections: Intersection): Intersections = Intersections().apply {
            addAll(intersections)
        }
    }
}
