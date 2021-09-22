package de.timo_reymann.mjml_support.error_reporting

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.Consumer
import org.apache.http.client.utils.URIBuilder
import java.awt.Component
import java.net.URI

class MjmlPluginErrorSubmitter : ErrorReportSubmitter() {
    companion object {
        const val REPO_SLUG = "timo-reymann/intellij-mjml-support"
        val pluginVersion by lazy {
            PluginManagerCore.getPlugin(PluginId.getId("de.timo_reymann.intellij-mjml-support"))
        }

        val systemInfo by lazy {
            "${SystemInfo.OS_NAME} ${SystemInfo.OS_VERSION} (${SystemInfo.OS_ARCH})"
        }

        val applicationInfo by lazy {
            ApplicationInfo.getInstance().let { "${it.fullApplicationName} (build ${it.build})" }
        }

        // http://www.faqs.org/rfcs/rfc2616.html
        const val URL_MAX_LENGTH = 2_000
    }

    fun generateGitHubIssueLink(
        title: String,
        description: String,
        additionalInformation: String,
        labels: List<String>
    ): String {
        val uriBuilder = URIBuilder()
        uriBuilder.scheme = "https"
        uriBuilder.host = "github.com"
        uriBuilder.path = "$REPO_SLUG/issues/new"
        uriBuilder.addParameter("title", title)
        uriBuilder.addParameter("labels", labels.joinToString(","))
        uriBuilder.addParameter(
            "body",
            """
            <!--- Please keep this note for the community --->
            ### Community Note

            * Please vote on this issue by adding a 👍 [reaction](https://blog.github.com/2016-03-10-add-reactions-to-pull-requests-issues-and-comments/) to the original issue to help the community and maintainers prioritize this request
            * Please do not leave "+1" or other comments that do not add relevant new information or questions, they generate extra noise for issue followers and do not help prioritize the request
            * If you are interested in working on this issue or have submitted a pull request, please leave a comment
            <!--- Thank you for keeping this note for the community --->

            ### Used versions

            <!-- Please include a list with all versions in the format 
            * Tool: version
            Every version that can be important should be listed here!
            -->
            * Host OS: $systemInfo
            * IDE: $applicationInfo
            * Plugin: ${pluginVersion!!.version}

            ## Problem description
            <!-- Describe what you are trying to do -->
            $description

            ## Expected Behavior

            <!--- What should have happened? --->

            ### Actual Behavior

            <!--- What actually happened? --->

            ### Steps to Reproduce

            <!--- Please list the steps required to reproduce the issue. --->


            ### Important Factoids
            <!--- Are there anything atypical about your environment/setup that we should know? --->
            $additionalInformation

            ### References

            <!---
            Information about referencing Github Issues: https://help.github.com/articles/basic-writing-and-formatting-syntax/#referencing-issues-and-pull-requests

            Are there any other GitHub issues (open or closed) or pull requests that should be linked here? Vendor documentation? For example:
            
            e.g.:
            * #0000
            --->
             """.split("\n")
                .joinToString("\n") { it.trimIndent() }
        )
        return uriBuilder.toString()
    }

    override fun getReportActionText(): String = "New GitHub Issue"

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val event = events.firstOrNull()
        val title = event?.throwableText?.lineSequence()?.first() ?: event?.message ?: ""
        val body = event?.throwableText ?: "<!-- Please paste the full stacktrace from the IDEA error popup. -->"
        val url: String
        try {
            url = generateGitHubIssueLink(
                title,
                additionalInfo ?: "",
                """
            #### StackTrace
            ```
            ${body.take(1_000)}
            ```
        """.trimIndent(), listOf("bug")
            )
        } catch (e: Exception) {
            consumer.consume(SubmittedReportInfo(null, null, SubmittedReportInfo.SubmissionStatus.FAILED))
            return false
        }

        // http://www.faqs.org/rfcs/rfc2616.html
        BrowserUtil.browse(URI(url.take(URL_MAX_LENGTH)))
        consumer.consume(
            SubmittedReportInfo(null, "Github Issue", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)
        )
        return true
    }
}
