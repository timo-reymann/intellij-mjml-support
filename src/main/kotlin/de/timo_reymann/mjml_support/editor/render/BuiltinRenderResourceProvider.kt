package de.timo_reymann.mjml_support.editor.render

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.io.Decompressor
import de.timo_reymann.mjml_support.editor.MjmlPreviewStartupActivity
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.editor.MjmlJCEFHtmlPanel

object BuiltinRenderResourceProvider {
    private var mjmlVersion: String = "?"
    private val mapper = jacksonObjectMapper()
    private val logger = logger<MjmlPreviewStartupActivity>()

    fun getBundledMjmlVersion(): String {
        return mjmlVersion
    }

    fun copyResources() {
        val rendererZip = FilePluginUtil.getFile(MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        FilePluginUtil.copyFile("node", MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        Decompressor.Zip(rendererZip)
            .extract(FilePluginUtil.getFile("renderer"))

        try {
            mjmlVersion = parseMjmlVersion()
        } catch (e: Exception) {
            mjmlVersion = "N/A"
            logger.warn("Failed to parse mjml version", e)
        }

        try {
            rendererZip.delete()
        } catch (e: Exception) {
            logger.warn("Failed to delete render zip", e)
        }

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_PREVIEW_FORCE_RENDER_TOPIC)
            .onForcedRender()
    }

    private fun parseMjmlVersion(): String {
        val packageJson = FilePluginUtil.getFile("renderer/package.json")
        return mapper.readTree(packageJson)["dependencies"]["mjml"].asText()
    }
}
