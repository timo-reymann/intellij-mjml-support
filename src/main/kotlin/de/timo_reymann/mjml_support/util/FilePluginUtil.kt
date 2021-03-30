package de.timo_reymann.mjml_support.util

import com.intellij.openapi.application.PluginPathManager
import com.intellij.util.ResourceUtil
import de.timo_reymann.mjml_support.editor.MjmlJCEFHtmlPanel
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets

object FilePluginUtil {
    val PLUGIN_HOME: File = PluginPathManager.getPluginHome("mjml-support")

    fun copyFile(path: String, fileName: String) {
        ResourceUtil.getResourceAsStream(MjmlJCEFHtmlPanel::class.java.classLoader, path, fileName).use { input ->
            val outFile = getFile(fileName)
            outFile.parentFile.mkdirs()
            outFile.createNewFile()
            FileWriter(outFile, false).use { output ->
                IOUtils.copy(input, output, StandardCharsets.UTF_8)
            }
        }
    }

    fun getFile(fileName: String): File {
        return File(PLUGIN_HOME, fileName)
    }
}
