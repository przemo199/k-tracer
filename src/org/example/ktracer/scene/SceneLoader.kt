package org.example.ktracer.scene

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlList
import java.io.IOException
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.readText
import kotlinx.serialization.SerializationException
import org.example.ktracer.composites.Camera
import org.example.ktracer.composites.World

data class Scene(val world: World, val camera: Camera)

/** A scene file that can't be read or is invalid. The message starts with `file:line:column:` where known. */
class SceneException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Loads YAML scene files: `SceneFormat.kt` describes the file, [SceneResolver] builds the world from it. */
object SceneLoader {
    private val yaml = Yaml.default

    fun load(path: Path): Scene {
        val text = try {
            path.readText()
        } catch (e: IOException) {
            throw SceneException("cannot read $path: ${if (e is NoSuchFileException) "no such file" else e.message}", e)
        }
        return parse(text, path.toString())
    }

    /** Parses a scene; [source] names it in error messages. */
    fun parse(text: String, source: String = "scene"): Scene {
        val entries = try {
            val root = yaml.parseToYamlNode(text)
            if (root !is YamlList) {
                throw YamlException("A scene is a list of `add` and `define` entries.", root.path)
            }
            root.items.map { Located(readEntry(it, yaml), it.location) }
        } catch (e: YamlException) {
            throw SceneException("$source:${e.line}:${e.column}: ${e.message}", e)
        } catch (e: SerializationException) {
            throw SceneException("$source: ${e.message}", e)
        }
        return SceneResolver(source).build(entries)
    }
}
