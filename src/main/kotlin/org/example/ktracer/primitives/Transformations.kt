package org.example.ktracer.primitives

import kotlin.math.cos
import kotlin.math.sin

object Transformations {
    @JvmStatic
    fun translation(x: Number, y: Number, z: Number): Transformation {
        val matrix = Transformation.IDENTITY
        matrix[0, 3] = x
        matrix[1, 3] = y
        matrix[2, 3] = z
        return matrix
    }

    @JvmStatic
    fun scaling(x: Number, y: Number, z: Number): Transformation {
        val matrix = Transformation.IDENTITY
        matrix[0, 0] = x
        matrix[1, 1] = y
        matrix[2, 2] = z
        return matrix
    }

    @JvmStatic
    fun rotationX(r: Number): Transformation {
        val matrix = Transformation.IDENTITY
        r.toDouble().also {
            val cosR = cos(it)
            val sinR = sin(it)
            matrix[1, 1] = cosR
            matrix[2, 1] = sinR
            matrix[1, 2] = -sinR
            matrix[2, 2] = cosR
        }
        return matrix
    }

    @JvmStatic
    fun rotationY(r: Number): Transformation {
        val matrix = Transformation.IDENTITY
        r.toDouble().also {
            val cosR = cos(it)
            val sinR = sin(it)
            matrix[0, 0] = cosR
            matrix[0, 2] = sinR
            matrix[2, 0] = -sinR
            matrix[2, 2] = cosR
        }
        return matrix
    }

    @JvmStatic
    fun rotationZ(r: Number): Transformation {
        val matrix = Transformation.IDENTITY
        r.toDouble().also {
            val cosR = cos(it)
            val sinR = sin(it)
            matrix[0, 0] = cosR
            matrix[0, 1] = -sinR
            matrix[1, 0] = sinR
            matrix[1, 1] = cosR
        }
        return matrix
    }

    @JvmStatic
    fun shearing(xy: Number, xz: Number, yx: Number, yz: Number, zx: Number, zy: Number): Transformation {
        val matrix = Transformation.IDENTITY
        matrix[0, 1] = xy
        matrix[0, 2] = xz
        matrix[1, 0] = yx
        matrix[1, 2] = yz
        matrix[2, 0] = zx
        matrix[2, 1] = zy
        return matrix
    }

    @JvmStatic
    fun viewTransform(from: Point, to: Point, up: Vector): Transformation {
        val forward = (to - from).normalized()
        val upNormalized = up.normalized()
        val leftVector = forward cross upNormalized
        val trueUp = leftVector cross forward
        val orientation = Matrix(
            listOf(
                leftVector.x, leftVector.y, leftVector.z, 0,
                trueUp.x, trueUp.y, trueUp.z, 0,
                -forward.x, -forward.y, -forward.z, 0,
                0, 0, 0, 1,
            ),
        )
        return orientation * translation(-from.x, -from.y, -from.z)
    }
}
