package org.example.ktracer

import com.sksamuel.scrimage.ImmutableImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import org.example.ktracer.composites.Camera
import org.example.ktracer.scene.SceneLoader
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Golden-image tests: every scene in `scenes/`, rendered [WIDTH] pixels wide and compared with its reference PNG in
 * `testResources/golden/`.
 *
 * After an intentional change to rendered output, regenerate the references with
 * `UPDATE_GOLDEN=1 ./kotlin test -m k-tracer --include-classes 'org.example.ktracer.GoldenImageTest'`, look at the
 * new images, and commit them together with the change. A failing scene's render is written to
 * `build/reports/golden/` (uploaded by CI with the other reports).
 */
class GoldenImageTest {
    @ParameterizedTest
    @MethodSource("scenes")
    fun `scene renders like its reference`(scene: Path) {
        val name = scene.nameWithoutExtension
        val actual = render(scene)
        val reference = GOLDEN.resolve("$name.png")
        if (System.getenv("UPDATE_GOLDEN") != null) {
            GOLDEN.createDirectories()
            Files.copy(actual, reference, StandardCopyOption.REPLACE_EXISTING)
            actual.deleteIfExists()
            return
        }
        assertTrue(reference.exists(), "$reference is missing; create it with UPDATE_GOLDEN=1 (see the class comment)")

        val expectedImage = ImmutableImage.loader().fromPath(reference)
        val actualImage = ImmutableImage.loader().fromPath(actual)
        assertEquals(expectedImage.width to expectedImage.height, actualImage.width to actualImage.height, "$name: size")
        val differences = expectedImage.pixels().zip(actualImage.pixels()) { expected, rendered ->
            max(abs(expected.red() - rendered.red()), max(abs(expected.green() - rendered.green()), abs(expected.blue() - rendered.blue())))
        }
        val outliers = differences.count { it > TOLERANCE }
        // rays grazing an edge may hit a different surface on another platform; allow a few
        val allowedOutliers = max(1, differences.size / 1000)
        if (outliers > allowedOutliers) {
            val kept = FAILURES.createDirectories().resolve("$name-actual.png")
            Files.move(actual, kept, StandardCopyOption.REPLACE_EXISTING)
            fail(
                "$name: $outliers pixels differ by more than $TOLERANCE (allowed $allowedOutliers, " +
                    "max difference ${differences.max()}). Rendered image: $kept",
            )
        }
        actual.deleteIfExists()
    }

    /** Renders [scene] at [WIDTH] pixels wide (keeping its aspect ratio) to a temporary PNG. */
    private fun render(scene: Path): Path {
        val (world, sceneCamera) = SceneLoader.load(scene)
        val height = (WIDTH * sceneCamera.verticalSize.toDouble() / sceneCamera.horizontalSize).roundToInt()
        val camera = Camera(WIDTH, height, sceneCamera.fieldOfView).apply { transformation = sceneCamera.transformation }
        val output = Files.createTempFile("golden-", ".png")
        camera.render(world, parallel = true).saveAsPng(output.toString())
        return output
    }

    companion {
        private const val WIDTH = 240

        /** Largest per-channel difference (out of 255) tolerated for floating-point differences between platforms. */
        private const val TOLERANCE = 2

        private val GOLDEN = Path("testResources/golden")
        private val FAILURES = Path("build/reports/golden")

        fun scenes(): Stream<Path> = Path("scenes").listDirectoryEntries("*.yaml").sorted().stream()
    }
}
