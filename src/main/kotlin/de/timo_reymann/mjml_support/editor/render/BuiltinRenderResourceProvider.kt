package de.timo_reymann.mjml_support.editor.render

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.io.Decompressor
import de.timo_reymann.mjml_support.editor.MjmlPreviewStartupActivity
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.editor.MjmlJCEFHtmlPanel

object BuiltinRenderResourceProvider {
    fun copyResources() {
        val rendererZip = FilePluginUtil.getFile(MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        FilePluginUtil.copyFile("node", MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        Decompressor.Zip(rendererZip)
            .extract(FilePluginUtil.getFile("renderer"))
        try {
            rendererZip.delete()
        } catch (e: Exception) {
            logger<MjmlPreviewStartupActivity>().warn("Failed to delete render zip", e)
        }

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_PREVIEW_FORCE_RENDER_TOPIC)
            .onForcedRender()
    }
}
