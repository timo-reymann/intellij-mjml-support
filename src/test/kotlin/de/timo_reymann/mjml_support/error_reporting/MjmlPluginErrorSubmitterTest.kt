package de.timo_reymann.mjml_support.error_reporting

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import org.junit.Assert
import org.junit.Test
import java.awt.Desktop

class MjmlPluginErrorSubmitterTest : MjmlPluginBaseTestCase() {
    @Test
    fun testGenerateGitHubIssueLink() {
        val submitter = MjmlPluginErrorSubmitter()
        val result = submitter.generateGitHubIssueLink("", "", "", listOf("bug"))
        Assert.assertNotNull(result)
        Desktop.getDesktop().browse(java.net.URI(result))
    }
}
