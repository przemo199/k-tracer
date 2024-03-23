package org.example.ktracer.primitives

import java.util.Objects
import org.example.ktracer.coarseEquals

abstract class Tuple(x: Number = 0, y: Number = 0, z: Number = 0) {
    val x: Double = x.toDouble()
    val y: Double = y.toDouble()
    val z: Double = z.toDouble()

    operator fun component1(): Double {
        return x
    }

    operator fun component2(): Double {
        return y
    }

    operator fun component3(): Double {
        return z
    }

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
