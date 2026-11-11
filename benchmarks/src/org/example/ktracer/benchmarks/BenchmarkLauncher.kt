package org.example.ktracer.benchmarks

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.zip.ZipFile
import javax.tools.ToolProvider
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.toPath
import kotlin.io.path.walk
import kotlin.io.path.writeLines
import kotlin.math.abs
import kotlin.system.exitProcess
import org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator

/**
 * Entry point of the `benchmarks` module (use `scripts\bench.cmd`, which pins the run to fixed cores):
 *
 * ```
 * kotlin run -m benchmarks -- [JMH filters and options] [--save-baseline NAME] [--baseline NAME]
 * ```
 *
 * 1. Generates the JMH harness from the compiled benchmark classes and compiles it with the JDK's javac. JMH's
 *    annotation processor only sees Java sources, so Kotlin benchmarks need its bytecode generator (as the JMH
 *    Gradle plugin does), and Kotlin Toolchain has no JMH integration.
 * 2. Runs `org.openjdk.jmh.Main` in a child JVM, with [FAST_CONFIG] unless an option is given explicitly.
 * 3. Like Criterion: `--save-baseline NAME` stores the scores, `--baseline NAME` compares against stored ones.
 *    Baselines live in `.benchmarks/baselines/` (git-ignored) rather than `build/`, so `kotlin clean` between the
 *    "before" and "after" runs does not delete them.
 */
fun main(args: Array<String>) {
    val arguments = LauncherArguments.parse(args.toList())
    val root = projectRoot()
    val workDir = root.resolve("build").resolve("benchmarks")
    val baselineDir = root.resolve(".benchmarks").resolve("baselines")
    val baseline = arguments.baseline?.let { name ->
        // fail before spending minutes on the run
        baselineFile(baselineDir, name).takeIf { it.exists() }
            ?: fail("no baseline '$name' in $baselineDir; record one first with --save-baseline $name")
    }
    val harnessClasses = generateHarness(workDir.resolve("harness"))

    val resultFile = workDir.resolve("latest.csv")
    resultFile.deleteIfExists()
    val jmhOptions = FAST_CONFIG.filterKeys { it !in arguments.jmh }.flatMap { (key, value) -> listOf(key, value) } +
            listOf("-rf", "csv", "-rff", resultFile.toString()) +
            arguments.jmh
    val java = Path(System.getProperty("java.home"), "bin", "java").toString()
    val classPath = harnessClasses.toString() + File.pathSeparator + System.getProperty("java.class.path")
    // English locale: JMH writes the CSV with locale-specific decimal separators
    val command = listOf(java, "-Duser.language=en", "-Duser.country=US", "-cp", classPath, "org.openjdk.jmh.Main") +
            jmhOptions
    val exitCode = ProcessBuilder(command).inheritIO().start().waitFor()
    if (exitCode != 0 || !resultFile.exists()) {
        exitProcess(exitCode) // failure, or a mode without results such as -l or -h
    }

    val results = Scores.read(resultFile)
    baseline?.let { compare(Scores.read(it), results, arguments.baseline) }
    arguments.saveBaseline?.let { name ->
        val file = baselineFile(baselineDir, name)
        val previous = if (file.exists()) Scores.read(file) else emptyMap()
        Scores.write(file, previous + results) // a filtered run only replaces the benchmarks it ran
        println("Saved ${results.size} scores to baseline '$name' ($file)")
    }
}

/**
 * Defaults for a reasonably quick but stable run (~4.5 s per benchmark, ~2.5 min in total): average time per input in
 * nanoseconds, 3 forks of 3 x 200 ms warm-up and 3 x 200 ms measurement iterations. Any option given on the command
 * line wins. Several forks matter more than long iterations: JIT decisions (inlining, escape analysis) differ between
 * JVMs, so with a single fork the same code moved by up to 27% between runs while JMH's error only covers one JVM.
 */
private val FAST_CONFIG = linkedMapOf(
    "-f" to "3",
    "-wi" to "3",
    "-w" to "200ms",
    "-i" to "3",
    "-r" to "200ms",
    "-bm" to "avgt",
    "-tu" to "ns",
    "-foe" to "true",
)

/**
 * Changes within this fraction are reported as "no change". Two pinned runs of unchanged code differed by 3.6% in the
 * median (unpinned: 5.5%), but individual allocation-heavy benchmarks moved by up to 29%; see [compare].
 */
private const val NOISE_THRESHOLD = 0.05

private class LauncherArguments(val jmh: List<String>, val saveBaseline: String?, val baseline: String?) {
    companion {
        fun parse(args: List<String>): LauncherArguments {
            val jmh = mutableListOf<String>()
            var saveBaseline: String? = null
            var baseline: String? = null
            val iterator = args.iterator()
            for (arg in iterator) {
                when (arg) {
                    "--save-baseline" -> saveBaseline = iterator.valueOf(arg)
                    "--baseline" -> baseline = iterator.valueOf(arg)
                    "-rf", "-rff" -> fail("$arg is set by the launcher; results go to build/benchmarks/latest.csv")
                    else -> jmh += arg
                }
            }
            return LauncherArguments(jmh, saveBaseline, baseline)
        }
    }
}

private fun Iterator<String>.valueOf(option: String): String = if (hasNext()) next() else fail("$option needs a name")

private fun fail(message: String): Nothing {
    System.err.println("error: $message")
    exitProcess(2)
}

/** The directory containing `project.yaml`, so paths do not depend on where `kotlin run` was started. */
private fun projectRoot(): Path {
    var directory: Path? = Path("").absolute()
    while (directory != null) {
        if (directory.resolve("project.yaml").exists()) return directory
        directory = directory.parent
    }
    return Path("").absolute()
}

private fun baselineFile(baselineDir: Path, name: String): Path {
    if (!name.matches(Regex("[A-Za-z0-9._-]+"))) fail("baseline name '$name' may only contain letters, digits, '.', '_' and '-'")
    return baselineDir.resolve("$name.csv")
}

/** Runs JMH's bytecode generator over the benchmark classes and compiles its output; returns the class directory. */
@OptIn(kotlin.io.path.ExperimentalPathApi::class)
private fun generateHarness(directory: Path): Path {
    directory.deleteRecursively() // drop harness classes of benchmarks that no longer exist
    val bytecode = directory.resolve("bytecode").createDirectories()
    val sources = directory.resolve("sources")
    val classes = directory.resolve("classes").createDirectories()

    val benchmarkCode = IntersectBenchmark::class.java.protectionDomain.codeSource.location.toURI().toPath()
    if (benchmarkCode.isDirectory()) {
        benchmarkCode.toFile().copyRecursively(bytecode.toFile())
    } else {
        extractClasses(benchmarkCode, bytecode)
    }
    JmhBytecodeGenerator.main(arrayOf(bytecode.toString(), sources.toString(), classes.toString(), "default"))

    val javac = ToolProvider.getSystemJavaCompiler() ?: fail("javac not found: benchmarks must run on a JDK, not a JRE")
    val javaFiles = sources.walk().filter { it.extension == "java" }.map(Path::toString).toList()
    val classPath = bytecode.toString() + File.pathSeparator + System.getProperty("java.class.path")
    val javacArgs = listOf("-d", classes.toString(), "-cp", classPath, "-proc:none", "-nowarn", "-encoding", "UTF-8")
    if (javac.run(null, null, null, *(javacArgs + javaFiles).toTypedArray()) != 0) {
        fail("compiling the generated JMH harness failed")
    }
    return classes
}

private fun extractClasses(jar: Path, target: Path) {
    ZipFile(jar.toFile()).use { zip ->
        for (entry in zip.entries()) {
            if (entry.isDirectory || !entry.name.endsWith(".class")) continue
            val file = target.resolve(entry.name).normalize()
            require(file.startsWith(target)) { "unexpected entry ${entry.name} in $jar" }
            file.parent.createDirectories()
            zip.getInputStream(entry).use { Files.copy(it, file) }
        }
    }
}

private data class Score(val mode: String, val score: Double, val error: Double, val unit: String)

private object Scores {
    private const val HEADER = "Benchmark,Mode,Score,Error,Unit"

    /** Reads JMH's CSV result format or a baseline written by [write], keyed by benchmark name and parameters. */
    fun read(file: Path): Map<String, Score> {
        if (!file.isRegularFile()) fail("no results at $file")
        val lines = file.readLines().filter(String::isNotBlank)
        val header = parseCsvLine(lines.first())
        val column = { name: String -> header.indexOf(name).also { check(it >= 0) { "$file has no '$name' column" } } }
        val benchmark = column("Benchmark")
        val mode = column("Mode")
        val score = column("Score")
        val error = header.indexOfFirst { it.startsWith("Score Error") }.takeIf { it >= 0 } ?: column("Error")
        val unit = column("Unit")
        val params = header.withIndex().filter { it.value.startsWith("Param: ") }
        return lines.drop(1).map(::parseCsvLine).associate { row ->
            val parameters = params.mapNotNull { [index, name] ->
                row[index].takeIf(String::isNotEmpty)?.let { "${name.removePrefix("Param: ")}=$it" }
            }
            // org.example.ktracer.benchmarks.VectorBenchmark.add -> VectorBenchmark.add
            val name = row[benchmark].split('.').takeLast(2).joinToString(".") +
                    if (parameters.isEmpty()) "" else parameters.joinToString(",", ":")
            name to Score(row[mode], row[score].toDouble(), row[error].toDoubleOrNull() ?: 0.0, row[unit])
        }
    }

    fun write(file: Path, scores: Map<String, Score>) {
        file.parent.createDirectories()
        file.writeLines(listOf(HEADER) + scores.toSortedMap().map { (name, s) ->
            listOf(name, s.mode, s.score.toString(), s.error.toString(), s.unit).joinToString(",") { quote(it) }
        })
    }

    private fun quote(value: String) = "\"" + value.replace("\"", "\"\"") + "\""

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val char = line[i]
            when {
                quoted && char == '"' && line.getOrNull(i + 1) == '"' -> { field.append('"'); i++ }
                char == '"' -> quoted = !quoted
                char == ',' && !quoted -> { fields += field.toString(); field.clear() }
                else -> field.append(char)
            }
            i++
        }
        fields += field.toString()
        return fields
    }
}

/**
 * Prints each benchmark's change against the baseline. Beyond [NOISE_THRESHOLD] it is "regressed"/"improved" only if
 * the 99.9% confidence intervals do not overlap; otherwise "within noise" (re-run before trusting it).
 */
private fun compare(baseline: Map<String, Score>, current: Map<String, Score>, name: String) {
    val nameWidth = current.keys.maxOf { it.length }
    fun row(benchmark: String, before: String, now: String, change: String, verdict: String) =
        "${benchmark.padEnd(nameWidth)}  ${before.padStart(14)}  ${now.padStart(14)}  ${change.padStart(8)}  $verdict"

    println()
    println("Comparison with baseline '$name' (noise threshold ${(NOISE_THRESHOLD * 100).toInt()}%):")
    println(row("Benchmark", "Baseline", "Current", "Change", "Verdict"))
    for ((benchmark, now) in current) {
        val before = baseline[benchmark]
        if (before == null || before.unit != now.unit) {
            println(row(benchmark, "-", now.format(), "", "no baseline"))
            continue
        }
        val change = (now.score - before.score) / before.score
        val lowerIsBetter = now.unit.endsWith("/op")
        val verdict = when {
            abs(change) <= NOISE_THRESHOLD -> "no change"
            abs(now.score - before.score) <= now.error + before.error -> "within noise"
            (change > 0) == lowerIsBetter -> "REGRESSED"
            else -> "improved"
        }
        println(row(benchmark, before.format(), now.format(), String.format(Locale.ROOT, "%+.1f%%", change * 100), verdict))
    }
}

private fun Score.format() = String.format(Locale.ROOT, "%.3f %s", score, unit)
