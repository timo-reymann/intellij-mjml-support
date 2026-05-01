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
import com.jetbrains.rd.util.debug
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.info
import com.jetbrains.rd.util.warn
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.settings.MjmlVersion
import de.timo_reymann.mjml_support.util.FilePluginUtil
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*

class NodeMjmlRenderer(project: Project) : BaseMjmlRenderer(project) {
    private val nodeMajorVersionCache = mutableMapOf<String, Int?>()

    override fun render(virtualFile: VirtualFile, text: String): String {
        val mjmlRenderParameters = MjmlRenderParameters(
            java.nio.file.Paths.get(virtualFile.path).parent.toString(),
            text,
            MjmlRenderParametersOptions(mjmlSettings.mjmlConfigFile),
            virtualFile.path,
            project.basePath
        )

        // One temp file per render. The renderer is project-scoped (single instance), so
        // sharing a single file across concurrent renders from multiple editors caused one
        // editor's preview to show another editor's HTML when their writes interleaved.
        val tempFile = File.createTempFile("mjml-render-${UUID.randomUUID()}", ".json")
        try {
            objectMapper.writeValue(tempFile, mjmlRenderParameters)
            val commandLine = generateCommandLine(virtualFile, tempFile)
            commandLine ?: return renderError(
                MjmlBundle.message("mjml_preview.node_not_configured"),
                MjmlBundle.message("mjml_preview.unavailable")
            )

            if (mjmlSettings.mjmlVersionEnum == MjmlVersion.V5) {
                val versionError = checkNodeVersionForV5(virtualFile)
                if (versionError != null) {
                    return versionError
                }
            }

            val rendererScript = getRendererScript()
            commandLine.withParameters(rendererScript)

            getLogger<NodeMjmlRenderer>().info {
                val selected = mjmlSettings.mjmlVersionEnum
                val bundled = BuiltinRenderResourceProvider.getBundledMjmlVersion(selected)
                val origin = if (mjmlSettings.useBuiltInNodeRenderer) "bundled" else "custom"
                "Rendering ${virtualFile.name} with MJML ${selected.id} ($origin, mjml=$bundled, script=$rendererScript)"
            }

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
        } finally {
            tempFile.delete()
        }
    }

    private fun generateCommandLine(virtualFile: VirtualFile, inputFile: File): GeneralCommandLine? {
        val nodeJsInterpreter = NodeJsInterpreterRef.createProjectRef().resolve(project) ?: return null
        val commandLine = GeneralCommandLine("node")
            .withInput(inputFile)
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
                    ProcessOutputType.STDERR -> {
                        val text = event.text
                        // MJML v5 emits informational "[MJML security]" warnings about includePath
                        // resolution that aren't real errors — keep them out of the IDE log.
                        if (text.contains("[MJML security]")) {
                            getLogger<NodeMjmlRenderer>().debug { text }
                        } else {
                            getLogger<NodeMjmlRenderer>().warn { text }
                        }
                    }
                }
            }
        })

        processHandler.startNotify()
        processHandler.waitFor()
        return Pair(processHandler.exitCode!!, buffer.toString())
    }

    private fun getRendererScript(): String {
        if (!mjmlSettings.useBuiltInNodeRenderer) {
            return mjmlSettings.renderScriptPath
        }
        return FilePluginUtil.getFile("${mjmlSettings.mjmlVersionEnum.dirName}/index.js").absolutePath
    }

    private fun checkNodeVersionForV5(virtualFile: VirtualFile): String? {
        val interpreter = NodeJsInterpreterRef.createProjectRef().resolve(project) ?: return null
        val cacheKey = interpreter.referenceName
        val major = nodeMajorVersionCache.getOrPut(cacheKey) { detectNodeMajorVersion(virtualFile) }
            ?: return null
        if (major < 20) {
            return renderError(
                MjmlBundle.message("mjml_preview.unavailable"),
                MjmlBundle.message("mjml_preview.node_version_too_low_for_v5", "v$major")
            )
        }
        return null
    }

    private fun detectNodeMajorVersion(virtualFile: VirtualFile): Int? {
        val nodeJsInterpreter = NodeJsInterpreterRef.createProjectRef().resolve(project) ?: return null
        val cmd = GeneralCommandLine("node")
            .withCharset(StandardCharsets.UTF_8)
            .withWorkDirectory(virtualFile.parent.toNioPath().toFile())
        NodeCommandLineConfigurator.find(nodeJsInterpreter).configure(cmd)
        cmd.withParameters("--version")

        val (exitCode, output) = captureOutput(cmd)
        if (exitCode != 0) return null
        // Output is like "v22.5.1\n"
        val trimmed = output.trim().removePrefix("v")
        return trimmed.substringBefore('.').toIntOrNull()
    }
}
