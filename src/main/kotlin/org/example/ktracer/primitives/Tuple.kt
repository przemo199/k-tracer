package org.example.ktracer.primitives

import java.util.Objects
import org.example.ktracer.coarseEquals

abstract class Tuple(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) {
    operator fun component1() = x

    operator fun component2() = y

    operator fun component3() = z

    override fun equals(other: Any?): Boolean {
        return other is Tuple &&
            x coarseEquals other.x &&
            y coarseEquals other.y &&
            z coarseEquals other.z
    }

    override fun hashCode(): Int {
        return Objects.hash(x, y, z)
    }

    abstract override fun toString(): String
}
