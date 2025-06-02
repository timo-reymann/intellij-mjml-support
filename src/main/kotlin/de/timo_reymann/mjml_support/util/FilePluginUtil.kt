package de.timo_reymann.mjml_support.util

import com.intellij.openapi.application.PathManager
import com.intellij.util.ResourceUtil
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.ui.MjmlJCEFHtmlPanel
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object FilePluginUtil {
    private val PLUGIN_HOME: File = File(PathManager.getPluginsPath(), MjmlBundle.message("technical_name"))
    const val NODE_RENDERER_ARCHIVE_NAME = "renderer.zip"
    const val WASI_RENDERER_WASM_NAME = "wasi/mrml-render.wasm";

    fun copyFile(path: String, fileName: String) {
        val destinationFile = getFile(fileName)
        destinationFile.parentFile.mkdirs()
        Files.copy(
            ResourceUtil.getResourceAsStream(FilePluginUtil::class.java.classLoader, path, fileName),
            destinationFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    fun getResource(path: Path): InputStream {
        return ResourceUtil.getResourceAsStream(
            FilePluginUtil::class.java.classLoader,
            path.parent.toString(),
            path.fileName.toString()
        )
    }

    fun getFile(fileName: String): File {
        return File(PLUGIN_HOME, fileName)
    }
}
