package de.timo_reymann.mjml_support.editor.render

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.io.Decompressor
import de.timo_reymann.mjml_support.editor.MjmlPreviewStartupActivity
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.editor.MjmlJCEFHtmlPanel
import de.timo_reymann.mjml_support.util.FileLockFailedException
import de.timo_reymann.mjml_support.util.FileLockUtil
import de.timo_reymann.mjml_support.util.MessageBusUtil

object BuiltinRenderResourceProvider {
    private var mjmlVersion: String = "?"
    private val mapper = jacksonObjectMapper()
    private val logger = logger<MjmlPreviewStartupActivity>()

    fun getBundledMjmlVersion(): String {
        return mjmlVersion
    }

    fun copyResources() {
        val rendererZip = FilePluginUtil.getFile(MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        val lockFile = FilePluginUtil.getFile(MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME + ".lock")

        try {
            FileLockUtil.runWithLock(lockFile) {
                FilePluginUtil.copyFile("node", MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
                Decompressor.Zip(rendererZip)
                    .extract(FilePluginUtil.getFile("renderer"))

                extractMjmlVersion()

                try {
                    rendererZip.delete()
                } catch (e: Exception) {
                    logger.warn("Failed to delete render zip", e)
                }

                notifyRendererResourcesChanged()
            }

        } catch (e: FileLockFailedException) {
            logger.warn("Failed to get file lock to copy renderer resources", e)
            MessageBusUtil.showMessage(
                NotificationType.WARNING,
                "Failed to copy resources for builtin mjml rendering",
                "This may be because another instance is already copying the files.<br/>" +
                        "You can also retry it manually under <b>Settings > Tools > MJML-Settings</b> | 'Copy renderer files for mjml preview'"
            )
        }
    }

    private fun notifyRendererResourcesChanged() {
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_PREVIEW_FORCE_RENDER_TOPIC)
            .onForcedRender()
    }

    private fun extractMjmlVersion() {
        try {
            mjmlVersion = parseMjmlVersion()
        } catch (e: Exception) {
            mjmlVersion = "N/A"
            logger.warn("Failed to parse mjml version", e)
        }
    }

    private fun parseMjmlVersion(): String {
        val packageJson = FilePluginUtil.getFile("renderer/package.json")
        return mapper.readTree(packageJson)["dependencies"]["mjml"].asText()
    }
}
