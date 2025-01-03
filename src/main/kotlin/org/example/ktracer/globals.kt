package org.example.ktracer

import kotlin.math.absoluteValue
import kotlin.math.sqrt

const val EPSILON = 0.00000008

const val MIN = -Double.MAX_VALUE

const val MAX = Double.MAX_VALUE

infix fun Double.coarseEquals(other: Double): Boolean {
    if (this == other) return true
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
