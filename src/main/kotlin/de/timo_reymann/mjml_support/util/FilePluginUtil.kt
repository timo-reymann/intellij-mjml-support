package de.timo_reymann.mjml_support.util

import com.intellij.openapi.application.PathManager
import com.intellij.util.ResourceUtil
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.MjmlJCEFHtmlPanel
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object FilePluginUtil {
    private val PLUGIN_HOME: File = File(PathManager.getPluginsPath(), MjmlBundle.message("technical_name"))

    fun copyFile(path: String, fileName: String) {
        val destinationFile = getFile(fileName)
        destinationFile.parentFile.mkdirs()
        Files.copy(
            ResourceUtil.getResourceAsStream(MjmlJCEFHtmlPanel::class.java.classLoader, path, fileName),
            destinationFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    fun getFile(fileName: String): File {
        return File(PLUGIN_HOME, fileName)
    }

}
