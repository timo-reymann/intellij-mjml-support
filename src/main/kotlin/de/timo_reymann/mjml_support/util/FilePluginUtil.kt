package de.timo_reymann.mjml_support.util

import com.intellij.openapi.application.PluginPathManager
import com.intellij.util.ResourceUtil
import de.timo_reymann.mjml_support.editor.MjmlJCEFHtmlPanel
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object FilePluginUtil {
    val PLUGIN_HOME: File = PluginPathManager.getPluginHome("mjml-support")

    fun copyFile(path: String, fileName: String) {
        Files.copy(
            ResourceUtil.getResourceAsStream(MjmlJCEFHtmlPanel::class.java.classLoader, path, fileName),
            getFile(fileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    fun getFile(fileName: String): File {
        return File(PLUGIN_HOME, fileName)
    }

}
