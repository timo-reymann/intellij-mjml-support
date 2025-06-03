package de.timo_reymann.mjml_support.editor.rendering

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.LogLevel
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.settings.MjmlSettings
import de.timo_reymann.mjml_support.util.MessageBusUtil
import java.io.File

abstract class BaseMjmlRenderer(
    internal val project: Project
) {
    internal val mjmlSettings: MjmlSettings
        get() = MjmlSettings.Companion.getInstance(project)
    internal val objectMapper = jacksonObjectMapper()

    init {
        objectMapper.registerModules(KotlinModule.Builder().build())
    }

    internal fun parseResult(rawJson: String): MjmlRenderResult {
        var renderResult: MjmlRenderResult
        try {
            renderResult = objectMapper.readValue(rawJson, MjmlRenderResult::class.java)
        } catch (e: Throwable) {
            getLogger<BaseMjmlRenderer>().warn {
                MjmlBundle.message("mjml_preview.render_parsing_failed", rawJson) + e.stackTraceToString()
            }
            renderResult = MjmlRenderResult()
            renderResult.errors = arrayOf()
            renderResult.stdout = rawJson
        }
        return renderResult
    }

    fun renderFragmentToHtml(virtualFile: VirtualFile, text: String) =
        renderToHtml(virtualFile, "<mjml><mj-body>$text</mj-body></mjml>")

    internal abstract fun render(virtualFile: VirtualFile, text: String): String

    fun renderToHtml(virtualFile: VirtualFile, text: String): String {
        val renderResult = parseResult(render(virtualFile, text))
        if (renderResult.html == null) {
            propagateErrorsToUser(virtualFile, renderResult)
            return renderError(
                MjmlBundle.message("mjml_preview.unavailable"),
                MjmlBundle.message("mjml_preview.unavailable_crash")
            )
        }

        val errors = renderResult.errors
            .filter { it.formattedMessage != null }
        if (errors.isNotEmpty()) {
            propagateErrorsToUser(virtualFile, renderResult)
        }

        val postProcessor = MjmlPostProcessor(virtualFile.parent.toNioPath(), mjmlSettings)
        try {
            return postProcessor.process(renderResult.html!!)
        } catch (e: Exception) {
            getLogger<BaseMjmlRenderer>().log(
                LogLevel.Warn,
                "Failed to replace image paths with post processor, returning unmodified html",
                e
            )
        }

        return renderResult.html ?: ""
    }

    internal fun propagateErrorsToUser(virtualFile: VirtualFile, result: MjmlRenderResult) {
        val message = result.errors
            .filter { it.formattedMessage != null }
            .joinToString("\n<br />") {
                """
                <a href="${virtualFile.path}:${it.line ?: 0}">
                    ${virtualFile.toNioPath().toFile().relativeTo(File(project.basePath!!))}:${it.line ?: 0}
                </a>: 
                ${it.message ?: "no error message available"}
                """.trimIndent()
            }

        val errorDetails = if (result.stdout == null) {
            ""
        } else {
            "<code${result.stdout}</code>"
        }

        val notification = Notification(
            MessageBusUtil.NOTIFICATION_GROUP,
            "<html><strong>${MjmlBundle.message("mjml_preview.render_failed")}</strong>${errorDetails}</html>",
            "<html>\n${message}</html>",
            NotificationType.WARNING
        )

        Notifications.Bus.notify(notification)
    }
}
