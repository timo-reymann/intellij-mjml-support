package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.io.Decompressor
import de.timo_reymann.mjml_support.util.FilePluginUtil

/**
 * Activity to copy editor files over
 */
class MjmlPreviewStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        val rendererZip = FilePluginUtil.getFile(MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        FilePluginUtil.copyFile("node", MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        Decompressor.Zip(rendererZip)
            .extract(FilePluginUtil.getFile("renderer"))
        try {
            rendererZip.delete()
        } catch (e: Exception) {
            logger<MjmlPreviewStartupActivity>().warn("Failed to delete render zip", e)
        }
    }
}
