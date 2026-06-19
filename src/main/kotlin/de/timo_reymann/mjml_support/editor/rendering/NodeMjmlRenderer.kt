package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.BaseProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRunOptions
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

        // The render parameters are fed to the renderer process via stdin (the bundled JS reads
        // fd 0). Each render starts its own process with its own stdin, so concurrent renders from
        // multiple editors can no longer interleave onto a shared resource.
        val paramsJson = objectMapper.writeValueAsString(mjmlRenderParameters)
        val targetRun = createTargetRun(virtualFile) ?: return renderError(
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
        targetRun.commandLineBuilder.addParameter(targetRun.path(rendererScript))

        getLogger<NodeMjmlRenderer>().info {
            val selected = mjmlSettings.mjmlVersionEnum
            val bundled = BuiltinRenderResourceProvider.getBundledMjmlVersion(selected)
            val origin = if (mjmlSettings.useBuiltInNodeRenderer) "bundled" else "custom"
            "Rendering ${virtualFile.name} with MJML ${selected.id} ($origin, mjml=$bundled, script=$rendererScript)"
        }

        val (exitCode, output) = captureOutput(targetRun, paramsJson)

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

    private fun createTargetRun(virtualFile: VirtualFile): NodeTargetRun? {
        val nodeJsInterpreter = NodeJsInterpreterRef.createProjectRef().resolve(project) ?: return null
        return try {
            val targetRun = NodeTargetRun(nodeJsInterpreter, project, null, NodeTargetRunOptions.of(false))
            targetRun.commandLineBuilder.charset = StandardCharsets.UTF_8
            targetRun.commandLineBuilder.setWorkingDirectory(virtualFile.parent.toNioPath().toString())
            targetRun
        } catch (e: ExecutionException) {
            getLogger<NodeMjmlRenderer>().warn { "Failed to set up node process: ${e.message}" }
            null
        }
    }

    private fun captureOutput(targetRun: NodeTargetRun, input: String?): Pair<Int, String> {
        val processHandler = targetRun.startProcess()
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
        // Feed the render parameters via stdin only after output draining has started, so a chatty
        // process can never fill the stdout pipe and deadlock while we are still writing.
        if (input != null) {
            writeToStdin(processHandler, input)
        }
        processHandler.waitFor()
        return Pair(processHandler.exitCode!!, buffer.toString())
    }

    private fun writeToStdin(processHandler: ProcessHandler, input: String) {
        val stdin = (processHandler as? BaseProcessHandler<*>)?.processInput ?: return
        stdin.use {
            it.write(input.toByteArray(StandardCharsets.UTF_8))
            it.flush()
        }
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
        val targetRun = createTargetRun(virtualFile) ?: return null
        targetRun.commandLineBuilder.addParameter("--version")

        val (exitCode, output) = captureOutput(targetRun, null)
        if (exitCode != 0) return null
        // Output is like "v22.5.1\n"
        val trimmed = output.trim().removePrefix("v")
        return trimmed.substringBefore('.').toIntOrNull()
    }
}
