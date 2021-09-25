package de.timo_reymann.mjml_support.util

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import de.timo_reymann.mjml_support.editor.ui.MjmlJCEFHtmlPanel
import org.apache.commons.io.FileUtils

class FilePluginUtilTest : MjmlPluginBaseTestCase() {
    fun testCopyFile() {
        FileUtils.deleteDirectory(FilePluginUtil.getFile(""))

        val file = FilePluginUtil.getFile(MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)

        // File should not exist now
        assertFalse(file.exists())

        // After copy file should be present
        FilePluginUtil.copyFile("node", MjmlJCEFHtmlPanel.RENDERER_ARCHIVE_NAME)
        assertTrue(file.exists())
    }
}
