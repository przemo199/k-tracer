package org.example.ktracer.composites

class Intersections : MutableList<Intersection> by ArrayList<Intersection>() {
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

//@JvmInline
//value class Intersections(val intersections: ArrayList<Intersection> = ArrayList()) {
//    val size
//        get() = intersections.size
//
//    fun hit(): Intersection? {
//        return intersections.filter { it.distance >= 0.0 }.minOrNull()
//    }
//
//    fun sort() {
//        intersections.sortBy { it.distance }
//    }
//
//    operator fun plusAssign(intersection: Intersection) {
//        intersections.add(intersection)
//    }
//
//    operator fun plusAssign(intersections: Intersections) {
//        this.intersections.addAll(intersections.intersections)
//    }
//
//    operator fun iterator(): Iterator<Intersection> {
//        return intersections.iterator()
//    }
//
//    fun clear() {
//        intersections.clear()
//    }
//
//    fun forEach(action: (Intersection) -> Unit) {
//        intersections.forEach(action)
//    }
//
//    fun isEmpty(): Boolean {
//        return intersections.isEmpty()
//    }
//
//    fun any(predicate: (Intersection) -> Boolean): Boolean {
//        return intersections.any(predicate)
//    }
//
//    operator fun get(index: Int): Intersection {
//        return intersections[index]
//    }
//
//    fun first(): Intersection {
//        return intersections.first()
//    }
//
//    companion object {
//        operator fun invoke(vararg intersections: Intersection): Intersections {
//            val a = Intersections()
//            intersections.forEach {
//                a += it
//            }
//            return a
//        }
//    }
//}
