package de.timo_reymann.mjml_support.editor

import com.intellij.ui.jcef.JCEFHtmlPanel
import com.intellij.util.io.Decompressor
import de.timo_reymann.mjml_support.util.FilePluginUtil
import org.apache.commons.lang.math.RandomUtils.nextInt

class MjmlJCEFHtmlPanel : JCEFHtmlPanel(getClassUrl()) {

    init {
        initRendererFiles()
    }

    private fun initRendererFiles() {
        FilePluginUtil.copyFile("node", RENDERER_ARCHIVE_NAME)
        val rendererZip = FilePluginUtil.getFile(RENDERER_ARCHIVE_NAME)
        Decompressor.Zip(rendererZip)
            .extract(FilePluginUtil.getFile("renderer"))
        rendererZip.delete()
    }

    companion object {
        internal const val RENDERER_ARCHIVE_NAME = "renderer.zip"

        private fun getClassUrl(): String {
            val url = try {
                val cls = MjmlJCEFHtmlPanel::class.java
                cls.getResource("${cls.simpleName}.class").toExternalForm() ?: error("Failed to get class URL!")
            } catch (ignored: Exception) {
                "about:blank"
            }
            return "$url@${nextInt(Integer.MAX_VALUE)}"
        }

    }
}
