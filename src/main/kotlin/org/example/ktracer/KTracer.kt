package org.example.ktracer

import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue
import org.example.ktracer.scene.SceneLoader

fun main(args: Array<String>) {
    val cliArgs = CliArgs.parseArgs(args)
    val (world, camera) = SceneLoader.parseYamlToScene(cliArgs.scenePath)
    println("Rendering image using ${cliArgs.scenePath} scene")
    val (canvas, duration) = measureTimedValue { camera.render(world, cliArgs.renderingMode == RenderingMode.PARALLEL) }
    println(String.format("Rendered image in %.3fs", duration.toDouble(DurationUnit.SECONDS)))
    canvas.saveAsPng(cliArgs.imageOutputPath)
}
