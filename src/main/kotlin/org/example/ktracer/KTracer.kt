package org.example.ktracer

import org.example.ktracer.scene.SceneLoader

fun main(args: Array<String>) {
    val cliArgs = CliArgs.parseArgs(args)
    val (world, camera) = SceneLoader.parseYamlToScene(cliArgs.scenePath)
    val canvas = camera.render(world, cliArgs.renderingMode == RenderingMode.PARALLEL)
    canvas.saveAsPng(cliArgs.imageOutputPath)
}
