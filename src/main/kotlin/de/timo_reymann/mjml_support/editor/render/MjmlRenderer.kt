package de.timo_reymann.mjml_support.editor.render

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
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
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.renderError
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.util.MessageBusUtil
import org.jetbrains.annotations.NotNull
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.event.HyperlinkEvent

class MjmlRenderer(private val project: Project, private val virtualFile: VirtualFile) {
    private val nodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()
    private val tempFile = File.createTempFile(UUID.randomUUID().toString(), "mjml")

    private fun resolveInterpreter(): NodeJsInterpreter? {
        return nodeJsInterpreterRef.resolve(project)
    }

    private fun generateCommandLine(nodeJsInterpreter: NodeJsInterpreter): @NotNull GeneralCommandLine {
        val commandLineConfigurator = NodeCommandLineConfigurator.find(nodeJsInterpreter)
        val commandLine = GeneralCommandLine("node", FilePluginUtil.getFile("renderer/index.js").absolutePath)
            .withInput(tempFile)
            .withWorkDirectory(File(virtualFile.path).parentFile)

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
            return null
        }
        return renderResult
    }

    fun render(text: String): String {
        Files.writeString(tempFile.toPath(), text, StandardCharsets.UTF_8)
        val nodeJsInterpreter = resolveInterpreter()
        nodeJsInterpreter ?: return renderError(
            MjmlBundle.message("mjml_preview.node_not_configured"),
            MjmlBundle.message("mjml_preview.unavailable")
        )

        val commandLine = generateCommandLine(nodeJsInterpreter)
        val (exitCode, output) = captureOutput(commandLine);

        if (exitCode != 0) {
            return renderError(
                MjmlBundle.message("mjml_preview.render_failed"),
                "<pre>$output</pre>"
            )
        }

        val renderResult = parseResult(output)
            ?: return renderError(
                MjmlBundle.message("mjml_preview.unavailable"),
                "Either your mjml is invalid or node.js is not set up properly"
            )

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
                }:${it.line}</a>: ${it.message}
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
                        println(event)
                    }
                })
            .notify(project)
    }
}
