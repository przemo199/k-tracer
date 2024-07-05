package org.example.ktracer.composites

class Intersections(vararg intersections: Intersection) : ArrayList<Intersection>(intersections.toList()) {
    fun hit(): Intersection? {
        return this.filter { it.distance >= 0.0 }.minOrNull()
    }
}

//@JvmInline
//value class Intersections2(val intersections: ArrayList<Intersection>) {
//    fun hit(): Intersection? {
//        var maybeHit: Intersection? = null
//        var hitDistance = MAX
//        intersections.forEach {
//            if (it.isWithinDistance(hitDistance)) {
//                maybeHit = it
//                hitDistance = it.distance
//            }
//        }
//        return maybeHit
//    }
//
//    fun sortByDistance() {
//        intersections.sortBy { it.distance }
//    }
//
//    operator fun plusAssign(intersection: Intersection) {
//        intersections.add(intersection)
//    }
//
//    operator fun plusAssign(intersections: Intersections2) {
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
//    companion object {
//        operator fun invoke(vararg intersection: Intersection): Intersections2 {
//            return Intersections2().apply { intersections += intersection }
//        }
//    }
//}
