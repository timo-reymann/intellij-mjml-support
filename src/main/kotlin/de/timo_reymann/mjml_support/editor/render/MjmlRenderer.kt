package de.timo_reymann.mjml_support.editor.render

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.error
import com.jetbrains.rd.util.getLogger
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.renderError
import de.timo_reymann.mjml_support.settings.MjmlSettings
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.util.MessageBusUtil
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.event.HyperlinkEvent

class MjmlRenderer(
    private val project: Project,
    private val virtualFile: VirtualFile,
    private val rendererScript: String = DEFAULT_RENDERER_SCRIPT
) {
    companion object {
        val DEFAULT_RENDERER_SCRIPT: String = FilePluginUtil.getFile("renderer/index.js").absolutePath
    }

    private val tempFile = File.createTempFile(UUID.randomUUID().toString(), "json")
    private val objectMapper = jacksonObjectMapper()
    private val mjmlRenderParameters =
        MjmlRenderParameters(project.basePath ?: File(virtualFile.path).parentFile.toString(), "")
    private val commandLine = generateCommandLine()
    private val mjmlSettings = MjmlSettings.getInstance(project)

    private fun updateTempFile(content: String) {
        mjmlRenderParameters.content = content
        objectMapper.writeValue(tempFile, mjmlRenderParameters)
    }

    private fun generateCommandLine(): GeneralCommandLine? {
        val nodeJsInterpreter = NodeJsInterpreterRef.createProjectRef().resolve(project) ?: return null
        val commandLine = GeneralCommandLine("node")
            .withInput(tempFile)
            .withWorkDirectory(File(virtualFile.path).parentFile)
        val commandLineConfigurator = NodeCommandLineConfigurator.find(nodeJsInterpreter)
        commandLineConfigurator.configure(commandLine)
        return commandLine
    }

    private fun captureOutput(commandLine: GeneralCommandLine): Pair<Int, String> {
        val line = AtomicInteger(0)
        val processHandler = OSProcessHandler(commandLine)
        val buffer = StringBuffer()
        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                // First line is command output, remove it
                if (line.incrementAndGet() == 1) {
                    return
                }
                buffer.append(event.text)
            }
        })

        processHandler.startNotify()
        processHandler.waitFor()
        return Pair(processHandler.exitCode!!, buffer.toString())
    }

    private fun parseResult(rawJson: String): MjmlRenderResult? {
        val mapper = jacksonObjectMapper()
        val renderResult: MjmlRenderResult
        try {
            renderResult = mapper.readValue(rawJson, MjmlRenderResult::class.java)
        } catch (e: Exception) {
            getLogger<MjmlRenderer>().error("Error parsing result from stdout: $rawJson", e)
            return null
        }
        return renderResult
    }

    fun render(text: String): String {
        updateTempFile(text)
        commandLine ?: return renderError(
            MjmlBundle.message("mjml_preview.node_not_configured"),
            MjmlBundle.message("mjml_preview.unavailable")
        )

        var script = DEFAULT_RENDERER_SCRIPT
        if (!mjmlSettings.useBuiltInRenderer) {
            script = mjmlSettings.renderScriptPath
        }
        commandLine.withParameters(script)

        val (exitCode, output) = captureOutput(commandLine);

        if (exitCode != 0) {
            return renderError(
                MjmlBundle.message("mjml_preview.render_failed"),
                "<pre>$output</pre>"
            )
        }

        val renderResult = parseResult(output)
        if (renderResult == null) {
            propagateErrorsToUser(listOf(MjmlRenderResultError(output)))
            return renderError(
                MjmlBundle.message("mjml_preview.unavailable"),
                "Either your mjml is invalid, node.js is not set up properly, or the rendering script crashed"
            )
        }


        val errors = renderResult.errors
            .filter { it.formattedMessage != null }
        if (errors.isNotEmpty()) {
            propagateErrorsToUser(errors)
        }

        return renderResult.html ?: ""
    }

    private fun propagateErrorsToUser(errors: List<MjmlRenderResultError>) {
        val message = errors
            .joinToString("\n<br />") {
                """
                            <a href="${virtualFile.path}:${it.line ?: 0}">${
                    virtualFile.toNioPath().toFile().relativeTo(File(project.basePath!!))
                }:${it.line ?: 0}</a>: ${it.message ?: "no error message available"}
                        """.trimIndent()
            }

        MessageBusUtil.NOTIFICATION_GROUP
            .createNotification(
                "<html><strong>${MjmlBundle.message("mjml_preview.render_failed")}</strong></html>",
                "<html>\n${message}</html>",
                NotificationType.WARNING,
                object : NotificationListener {
                    override fun hyperlinkUpdate(notification: Notification, event: HyperlinkEvent) {
                        if (event.eventType != HyperlinkEvent.EventType.ACTIVATED) {
                            return
                        }

                        val fields = event.description.split(":")
                        val file = VfsUtil.findFile(File(fields[0]).toPath(), true)
                        FileEditorManager.getInstance(project)
                            .openTextEditor(OpenFileDescriptor(project, file!!, fields[1].toInt(), 0), true)
                    }
                })
            .notify(project)
    }
}
