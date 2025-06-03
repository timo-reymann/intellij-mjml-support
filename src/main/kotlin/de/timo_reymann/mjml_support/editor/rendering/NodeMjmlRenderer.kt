package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.util.FilePluginUtil
import okio.Path.Companion.toPath
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

class NodeMjmlRenderer(project: Project) : BaseMjmlRenderer(project) {
    companion object {
        val DEFAULT_RENDERER_SCRIPT: String = FilePluginUtil.getFile("renderer/index.js").absolutePath
    }

    private val tempFile = File.createTempFile(UUID.randomUUID().toString(), "json")

    override fun render(virtualFile: VirtualFile, text: String): String {
        val mjmlRenderParameters = MjmlRenderParameters(
            virtualFile.path.toPath().parent.toString(),
            text,
            MjmlRenderParametersOptions(mjmlSettings.mjmlConfigFile),
            virtualFile.path
        )

        objectMapper.writeValue(tempFile, mjmlRenderParameters)
        val commandLine = generateCommandLine(virtualFile)
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
                    MjmlBundle.message(if (mjmlSettings.useBuiltInNodeRenderer) "mjml_preview.renderer_copying" else "mjml_preview.renderer_missing"),
                    "<pre>${MjmlBundle.message("mjml_preview.renderer_preview_will_reload")}</pre>"
                )
            }

            return renderError(
                MjmlBundle.message("mjml_preview.render_failed"),
                "<pre>$output</pre>"
            )
        }
        return output
    }

    private fun generateCommandLine(virtualFile: VirtualFile): GeneralCommandLine? {
        val nodeJsInterpreter = NodeJsInterpreterRef.createProjectRef().resolve(project) ?: return null
        val commandLine = GeneralCommandLine("node")
            .withInput(tempFile)
            .withCharset(StandardCharsets.UTF_8)
            .withWorkDirectory(virtualFile.parent.toNioPath().toFile())
        val commandLineConfigurator = NodeCommandLineConfigurator.find(nodeJsInterpreter)
        commandLineConfigurator.configure(commandLine)
        return commandLine
    }

    private fun captureOutput(commandLine: GeneralCommandLine): Pair<Int, String> {
        val processHandler = OSProcessHandler(commandLine)
        val buffer = StringBuffer()
        processHandler.addProcessListener(object : ProcessListener {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                when (outputType) {
                    ProcessOutputType.STDOUT -> buffer.append(event.text)
                    // stderr may contain duplicates from errors (due to hard coded console.error in mjml code)
                    ProcessOutputType.STDERR -> getLogger<NodeMjmlRenderer>().warn { event.text }
                }
            }
        })

        processHandler.startNotify()
        processHandler.waitFor()
        return Pair(processHandler.exitCode!!, buffer.toString())
    }

    private fun getRendererScript(): String {
        var script = DEFAULT_RENDERER_SCRIPT
        if (!mjmlSettings.useBuiltInNodeRenderer) {
            script = mjmlSettings.renderScriptPath
        }
        return script
    }
}
