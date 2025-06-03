package de.timo_reymann.mjml_support.editor.render

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
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
import de.timo_reymann.mjml_support.wasi.WasiLoader
import de.timo_reymann.mjml_support.wasi.WasiLoaderOptions
import okio.Path.Companion.toPath
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.relativeTo


abstract class BaseMjmlRenderer(
    internal val project: Project
) {
    internal val mjmlSettings: MjmlSettings
        get() = MjmlSettings.getInstance(project)
    internal val objectMapper = jacksonObjectMapper()

    init {
        objectMapper.registerModules(KotlinModule.Builder().build())
    }

    internal fun parseResult(rawJson: String): MjmlRenderResult {
        var renderResult: MjmlRenderResult
        try {
            renderResult = objectMapper.readValue(rawJson, MjmlRenderResult::class.java)
        } catch (e: Throwable) {
            getLogger<NodeMjmlRenderer>().warn {
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
            getLogger<NodeMjmlRenderer>().log(
                com.jetbrains.rd.util.LogLevel.Warn,
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
                    STDOUT -> buffer.append(event.text)
                    // stderr may contain duplicates from errors (due to hard coded console.error in mjml code)
                    STDERR -> getLogger<NodeMjmlRenderer>().warn { event.text }
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

class WasiMjmlRenderer(project: Project) : BaseMjmlRenderer(project) {
    private val stringTerminator = "\u0000"
    private val wasiInstance by lazy {
        val loader = WasiLoader(
            WasiLoaderOptions(
                rootHostPath = Path(project.basePath!!)
            )
        )
        val moduleBuilder = loader.createModuleBuilder(BuiltinRenderResourceProvider.getBuiltinWasiRenderer())
        moduleBuilder.build()
    }


    override fun render(virtualFile: VirtualFile, text: String): String {
        val mjmlRenderParameters = MjmlRenderParameters(
            "/" + virtualFile.toNioPath().parent.relativeTo(Path(project.basePath!!)).toString(),
            text,
            MjmlRenderParametersOptions(mjmlSettings.mjmlConfigFile),
            virtualFile.path
        )

        val renderMjml = wasiInstance.export("render_mjml")
        val freeString = wasiInstance.export("free_string")
        val allocString = wasiInstance.export("alloc_string")
        val memory = wasiInstance.memory()

        val raw = objectMapper.writeValueAsString(mjmlRenderParameters) + stringTerminator

        // Allocate memory for raw and write into memory
        val rawAllocPtr = allocString.apply(raw.length.toLong())
        val rawPtr = rawAllocPtr[0].toInt()
        memory.write(rawPtr, raw.toByteArray())

        // Call render
        val resultPtr = renderMjml.apply(rawPtr.toLong())

        // Read characters until termination
        var character = -1
        val characters: MutableList<Byte?> = ArrayList<Byte?>()
        var offset = resultPtr[0].toInt()
        while (character != 0) {
            character = memory.read(offset).toInt()
            offset++
            if (character != 0) {
                characters.add(character.toByte())
            }
        }

        // Free memory for the return pointer
        freeString.apply(resultPtr[0])

        // Free memory for the input pointer
        freeString.apply(rawAllocPtr[0])

        // Convert into the byte array
        val byteArray = ByteArray(characters.size)
        for (j in characters.indices) {
            byteArray[j] = characters.get(j)!!
        }
        return String(byteArray, StandardCharsets.UTF_8)
    }
}
