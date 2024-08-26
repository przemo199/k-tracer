package org.example.ktracer

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue
import org.example.ktracer.scene.SceneLoader

class KTracer : CliktCommand(help = "Render scene to image", printHelpOnEmptyArgs = true) {
    val scenePath by argument()
    val imageOutputPath by argument()
    val renderingMode by option("--rendering-mode", "-r").enum<RenderingMode>().default(RenderingMode.PARALLEL)

    override fun run() {
        val (world, camera) = SceneLoader.parseYamlToScene(scenePath)
        println("Rendering image using $scenePath scene")
        val (canvas, duration) = measureTimedValue { camera.render(world, RenderingMode.PARALLEL == renderingMode) }
        println(String.format("Rendered image in %.3fs", duration.toDouble(DurationUnit.SECONDS)))
        canvas.saveAsPng(imageOutputPath)
    }
}

enum class RenderingMode {
    SERIAL,
    PARALLEL,
}

fun main(args: Array<String>) = KTracer().main(args)
