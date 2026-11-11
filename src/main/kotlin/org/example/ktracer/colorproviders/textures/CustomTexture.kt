package org.example.ktracer.colorproviders.textures

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import java.nio.file.Path
import org.example.ktracer.colorproviders.mappings.Mapping
import org.example.ktracer.primitives.Color
import org.example.ktracer.primitives.Coordinates
import org.example.ktracer.primitives.Transformation

class CustomTexture(
    val path: Path,
    mapping: Mapping,
    transformation: Transformation = Transformation.identity()
) : Texture(mapping, transformation) {
    val texture: ImmutableImage = ImmutableImage.loader().fromPath(path)

    override fun colorAt(coordinates: Coordinates): Color {
        val x = (coordinates.u * texture.width).toInt()
        val y = (coordinates.v * texture.height).toInt()
        return texture.color(x, y).toColor()
    }

    override fun toString(): String = "CustomTexture(path=$path)"

    private fun RGBColor.toColor(): Color =
        Color(red / MAX_COLOR_VALUE, green / MAX_COLOR_VALUE, blue / MAX_COLOR_VALUE)

    companion {
        private const val MAX_COLOR_VALUE = 255.0
    }
}
