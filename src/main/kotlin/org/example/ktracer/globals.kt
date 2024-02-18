package org.example.ktracer

import kotlin.math.absoluteValue
import kotlin.math.sqrt

const val EPSILON = 0.000000008

const val MIN = -Double.MAX_VALUE

const val MAX = Double.MAX_VALUE

const val MAX_REFLECTION_ITERATIONS = 6

infix fun Double.coarseEquals(other: Double): Boolean {
    return (this - other).absoluteValue < EPSILON
}

fun Double.squared(): Double {
    return this * this
}

fun solveQuadratic(a: Double, b: Double, c: Double): Pair<Double, Double>? {
    val discriminant = b.squared() - 4.0 * a * c
    if (discriminant < 0.0) {
        return null
    }
    val doubleA = 2.0 * a
    val discriminantRoot = sqrt(discriminant)
    val solution1 = (-b - discriminantRoot) / doubleA
    val solution2 = (-b + discriminantRoot) / doubleA
    return Pair(solution1, solution2)
}
