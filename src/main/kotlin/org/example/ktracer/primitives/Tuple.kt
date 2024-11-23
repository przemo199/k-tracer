package org.example.ktracer.primitives

import java.util.Objects
import kotlin.math.min
import kotlin.math.max
import org.example.ktracer.coarseEquals

abstract class Tuple(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) : Iterable<Double> {
    abstract val w: Double

    operator fun component1() = x

    operator fun component2() = y

    operator fun component3() = z

    operator fun component4(): Double = w

    operator fun contains(value: Double): Boolean {
        return x coarseEquals value || y coarseEquals value || z coarseEquals value
    }

    operator fun get(index: Int) = when (index) {
        0 -> x
        1 -> y
        2 -> z
        3 -> component4()
        else -> throw IndexOutOfBoundsException(index)
    }

    fun toDoubleArray(): DoubleArray {
        return doubleArrayOf(x, y, z)
    }

    fun min(): Double {
        return min(x, min(y, z))
    }

    fun max(): Double {
        return max(x, max(y, z))
    }

    override fun iterator(): DoubleIterator {
        return TupleIterator(this)
    }

    override fun equals(other: Any?): Boolean {
        return other is Tuple &&
            x coarseEquals other.x &&
            y coarseEquals other.y &&
            z coarseEquals other.z
    }

    override fun hashCode(): Int {
        return Objects.hash(x, y, z, w)
    }

    abstract override fun toString(): String

    companion object {
        const val SIZE = 4
    }
}

private class TupleIterator(private val tuple: Tuple) : DoubleIterator() {
    private var index = 0

    override fun hasNext(): Boolean = index < Tuple.SIZE

    override fun nextDouble(): Double {
        try {
            return tuple[index++]
        } catch (e: IndexOutOfBoundsException) {
            index -= 1
            throw NoSuchElementException(e.message, e)
        }
    }
}
