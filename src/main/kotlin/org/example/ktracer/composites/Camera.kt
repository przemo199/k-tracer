package org.example.ktracer.composites

import dev.reimer.progressbar.ktx.progressBar
import me.tongfei.progressbar.ProgressBarStyle
import org.example.ktracer.primitives.Point
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.tan
import org.example.ktracer.primitives.Transformation

class Camera {
    val horizontalSize: Int
    val verticalSize: Int
    val fieldOfView: Double
    val halfWidth: Double
    val halfHeight: Double
    val pixelSize: Double
    var transformation: Transformation
        get() {
            return transformationInverse.inverse()
        }
        set(value) {
            transformationInverse = value.inverse()
        }
    var transformationInverse: Transformation
        private set

    constructor(horizontalSize: Int, verticalSize: Int, fieldOfView: Number) {
        val halfView = tan(fieldOfView.toDouble() / 2.0)
        val aspect = horizontalSize.toDouble() / verticalSize.toDouble()
        val halfWidth: Double
        val halfHeight: Double

        if (aspect >= 1.0) {
            halfWidth = halfView
            halfHeight = halfView / aspect
        } else {
            halfWidth = halfView * aspect
            halfHeight = halfView
        }
        val pixelSize = (halfWidth * 2.0) / horizontalSize.toDouble()
        this.horizontalSize = horizontalSize
        this.verticalSize = verticalSize
        this.fieldOfView = fieldOfView.toDouble()
        this.halfWidth = halfWidth
        this.halfHeight = halfHeight
        this.pixelSize = pixelSize
        this.transformationInverse = Transformation.IDENTITY
    }

    fun rayForPixel(x: Int, y: Int): Ray {
        val offsetX = (x + 0.5) * pixelSize
        val offsetY = (y + 0.5) * pixelSize
        val worldX = halfWidth - offsetX
        val worldY = halfHeight - offsetY
        val pixel = transformationInverse * Point(worldX, worldY, -1.0)
        val origin = transformationInverse * Point.ORIGIN
        val direction = (pixel - origin).normalized()
        return Ray(origin, direction)
    }

    fun render(world: World, parallel: Boolean = false): Canvas {
        val canvas = Canvas(horizontalSize, verticalSize)
        IntStream.range(0, canvas.size).run {
            if (parallel) this.parallel() else this
        }.wrapInProgressBar().forEach { index ->
            val x = index % canvas.width
            val y = index / canvas.width
            val ray = rayForPixel(x, y)
            canvas[index] = world.colorAt(ray)
        }
        return canvas
    }

    fun renderParallel(world: World): Canvas {
        return render(world, true)
    }

    override fun toString(): String {
        return "Camera(horizontalSize=$horizontalSize, verticalSize=$verticalSize, fieldOfView=$fieldOfView, halfWidth=$halfWidth, halfHeight=$halfHeight, pixelSize=$pixelSize, transformation=$transformation)"
    }

    companion object {
        @JvmStatic
        fun IntStream.wrapInProgressBar(): Stream<Int> {
            return this.progressBar {
                unitSize = 1
                style = ProgressBarStyle.ASCII
                updateIntervalMillis = 50
            }
        }
    }
}
