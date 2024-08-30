package de.timo_reymann.mjml_support.editor.render

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputType.STDERR
import com.intellij.execution.process.ProcessOutputType.STDOUT
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.settings.MjmlSettings
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.util.MessageBusUtil
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

class MjmlRenderer(
    private val project: Project,
    private val virtualFile: VirtualFile
) {
    companion object {
        val DEFAULT_RENDERER_SCRIPT: String = FilePluginUtil.getFile("renderer/index.js").absolutePath
    }

    private val mjmlSettings = MjmlSettings.getInstance(project)
    private val tempFile = File.createTempFile(UUID.randomUUID().toString(), "json")
    private val objectMapper = jacksonObjectMapper()
    private val basePath by lazy {
        File(virtualFile.path).parentFile
    }

    private val postProcessor = MjmlPostProcessor(basePath, mjmlSettings)
    private val mjmlRenderParameters =
        MjmlRenderParameters(
            basePath.toString(),
            "",
            MjmlRenderParametersOptions(mjmlSettings.mjmlConfigFile),
            virtualFile.path
        )

    private fun updateTempFile(content: String) {
        mjmlRenderParameters.content = content
        objectMapper.writeValue(tempFile, mjmlRenderParameters)
    }

    private fun generateCommandLine(): GeneralCommandLine? {
        val nodeJsInterpreter = NodeJsInterpreterRef.createProjectRef().resolve(project) ?: return null
        val commandLine = GeneralCommandLine("node")
            .withInput(tempFile)
            .withCharset(StandardCharsets.UTF_8)
            .withWorkDirectory(basePath)
        val commandLineConfigurator = NodeCommandLineConfigurator.find(nodeJsInterpreter)
        commandLineConfigurator.configure(commandLine)
        return commandLine
    }

    private fun captureOutput(commandLine: GeneralCommandLine): Pair<Int, String> {
        val processHandler = OSProcessHandler(commandLine)
        val buffer = StringBuffer()
        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                when (outputType) {
                    STDOUT -> buffer.append(event.text)
                    // stderr may contain duplicates from errors (due to hard coded console.error in mjml code)
                    STDERR -> getLogger<MjmlRenderer>().warn { event.text }
                }
            }
        })

        processHandler.startNotify()
        processHandler.waitFor()
        return Pair(processHandler.exitCode!!, buffer.toString())
    }

    private fun parseResult(rawJson: String): MjmlRenderResult {
        val mapper = jacksonObjectMapper()
        var renderResult: MjmlRenderResult
        try {
            renderResult = mapper.readValue(rawJson, MjmlRenderResult::class.java)
        } catch (e: Throwable) {
            getLogger<MjmlRenderer>().warn {
                MjmlBundle.message("mjml_preview.render_parsing_failed", rawJson) + e.stackTraceToString()
            }
            renderResult = MjmlRenderResult()
            renderResult.errors = arrayOf()
            renderResult.stdout = rawJson
        }
        return renderResult
    }

    private fun getRendererScript(): String {
        var script = DEFAULT_RENDERER_SCRIPT
        if (!mjmlSettings.useBuiltInRenderer) {
            script = mjmlSettings.renderScriptPath
        }
        return script
    }

    fun renderFragment(text: String) = render("<mjml><mj-body>$text</mj-body></mjml>")

    fun render(text: String): String {
        updateTempFile(text)
        val commandLine = generateCommandLine()
        commandLine ?: return renderError(
            MjmlBundle.message("mjml_preview.node_not_configured"),
            MjmlBundle.message("mjml_preview.unavailable")
        )

        val rendererScript = getRendererScript()
        commandLine.withParameters(rendererScript)

        val (exitCode, output) = captureOutput(commandLine)

        if (exitCode != 0) {
            if (!File(rendererScript).exists()) {
                return renderError(
                    MjmlBundle.message(if (mjmlSettings.useBuiltInRenderer) "mjml_preview.renderer_copying" else "mjml_preview.renderer_missing"),
                    "<pre>${MjmlBundle.message("mjml_preview.renderer_preview_will_reload")}</pre>"
                )
            }

            return renderError(
                MjmlBundle.message("mjml_preview.render_failed"),
                "<pre>$output</pre>"
            )
        }

        val renderResult = parseResult(output)
        if (renderResult.html == null) {
            propagateErrorsToUser(renderResult)
            return renderError(
                MjmlBundle.message("mjml_preview.unavailable"),
                MjmlBundle.message("mjml_preview.unavailable_crash")
            )
        }

        val errors = renderResult.errors
            .filter { it.formattedMessage != null }
        if (errors.isNotEmpty()) {
            propagateErrorsToUser(renderResult)
        }

        try {
            return postProcessor.process(renderResult.html!!)
        } catch (e: Exception) {
            getLogger<MjmlRenderer>().log(
                com.jetbrains.rd.util.LogLevel.Warn,
                "Failed to replace image paths with post processor, returning unmodified html",
                e
            )
        }

        return renderResult.html ?: ""
    }

    private fun propagateErrorsToUser(result: MjmlRenderResult) {
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
