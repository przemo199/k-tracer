package org.example.ktracer

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

class CliArgs {
    val parser = ArgParser("ktracer")
    val scenePath by parser.argument(ArgType.String, "scenePath")
    val imageOutputPath by parser.argument(ArgType.String, "imageOutputPath")
    val renderingMode by parser.option(ArgType.Choice<RenderingMode>(), "renderingMode", shortName = "r")
        .default(RenderingMode.PARALLEL)

    private fun parse(args: Array<String>) {
        parser.parse(args)
    }

    companion object {
        @JvmStatic
        fun parseArgs(args: Array<String>): CliArgs {
            val arguments = CliArgs()
            arguments.parse(args)
            return arguments
        }
    }
}

enum class RenderingMode {
    SERIAL,
    PARALLEL,
}
