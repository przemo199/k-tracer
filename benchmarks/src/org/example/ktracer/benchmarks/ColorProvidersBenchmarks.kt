package org.example.ktracer.benchmarks

import org.example.ktracer.colorproviders.ColorProvider
import org.example.ktracer.colorproviders.mappings.CylindricalMapping
import org.example.ktracer.colorproviders.mappings.PlanarMapping
import org.example.ktracer.colorproviders.mappings.SphericalMapping
import org.example.ktracer.colorproviders.patterns.CheckerPattern
import org.example.ktracer.colorproviders.patterns.ComplexPattern
import org.example.ktracer.colorproviders.patterns.GradientPattern
import org.example.ktracer.colorproviders.patterns.RingPattern
import org.example.ktracer.colorproviders.patterns.StripePattern
import org.example.ktracer.colorproviders.textures.CubeMap
import org.example.ktracer.colorproviders.textures.TextureMap
import org.example.ktracer.colorproviders.textures.UvAlignCheck
import org.example.ktracer.colorproviders.textures.UvCheckers
import org.example.ktracer.colorproviders.textures.UvImage
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Point
import org.example.ktracer.primitives.Transformations
import org.example.ktracer.shapes.Sphere
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

// Surface colors: 3D patterns and texture maps, as `Material.lighting` asks for them on every shaded hit.

/** Every color provider in the benchmark, by `ColorProviderBenchmark.provider` name. */
fun colorProviders(): Map<String, ColorProvider> {
    val rng = Rng(20)
    val scale = Transformations.scaling(0.3, 0.3, 0.3)
    val checkers = UvCheckers(16, 8, Color.WHITE, Color.BLACK)
    fun alignCheck() = UvAlignCheck(rng.color(), Color.RED, Color.GREEN, Color.BLUE, Color.WHITE)
    val imageSize = 256
    val image = UvImage(imageSize, imageSize, IntArray(imageSize * imageSize) { rng.range(0.0, 16777216.0).toInt() })
    return mapOf(
        "stripes" to StripePattern(Color.WHITE, Color.BLACK, scale),
        "checkers" to CheckerPattern(Color.WHITE, Color.BLACK, scale),
        "gradient" to GradientPattern(Color.WHITE, Color.BLACK, scale),
        "rings" to RingPattern(Color.WHITE, Color.BLACK, scale),
        "nested" to ComplexPattern(
            StripePattern(Color.WHITE, Color.BLACK, Transformations.rotationY(1.0)),
            RingPattern(Color.RED, Color.BLUE, scale),
            scale,
        ),
        "map-planar" to TextureMap(checkers, PlanarMapping),
        "map-spherical" to TextureMap(checkers, SphericalMapping),
        "map-cylindrical" to TextureMap(checkers, CylindricalMapping),
        "map-image" to TextureMap(image, SphericalMapping),
        "cube-map" to CubeMap(alignCheck(), alignCheck(), alignCheck(), alignCheck(), alignCheck(), alignCheck()),
    )
}

@State(Scope.Thread)
@OperationsPerInvocation(BATCH)
class ColorProviderBenchmark {
    @Param(
        "stripes", "checkers", "gradient", "rings", "nested",
        "map-planar", "map-spherical", "map-cylindrical", "map-image", "cube-map",
    )
    lateinit var provider: String

    private lateinit var colorProvider: ColorProvider
    private lateinit var points: Array<Point>

    // transformed, so that the shape's and the provider's transformations both cost what they do in a scene
    private val shape = Sphere(
        transformation = Transformations.translation(1.0, 0.5, 0.0) * Transformations.scaling(2.0, 2.0, 2.0),
    )
    private val colors = arrayOfNulls<Color>(BATCH)

    @Setup
    fun setUp() {
        val providers = colorProviders()
        // points on the sphere's surface, in world space
        points = Rng(21).batch { shape.transformation * unitVector().toPoint() }
        // A scene mixes providers at `Material.lighting`'s call site; without this, each JMH fork would see one
        // provider type and the JIT would inline it.
        repeat(20) {
            for (other in providers.values) {
                for (point in points) other.colorAtShape(shape, point)
            }
        }
        colorProvider = providers.getValue(provider)
    }

    @Benchmark
    fun colorAtShape(): Array<Color?> {
        for (i in 0 until BATCH) colors[i] = colorProvider.colorAtShape(shape, points[i])
        return colors
    }
}
