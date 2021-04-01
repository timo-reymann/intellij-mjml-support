package de.timo_reymann.mjml_support.editor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.CommonBundle
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
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
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.intellij.util.Alarm
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.util.MessageBusUtil
import org.intellij.plugins.markdown.ui.split.SplitFileEditor
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeListener
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.HyperlinkEvent


class MjmlPreviewFileEditor(private val project: Project, private val virtualFile: VirtualFile) :
    UserDataHolderBase(), FileEditor {
    private val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)
    private val tempFile = File.createTempFile("abc", "def")
    private val htmlPanelWrapper: JPanel
    private var panel: JCEFHtmlPanel? = null
    private val pooledAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val swingAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private val REQUESTS_LOCK = Any()
    private var lastHtmlOrRefreshRequest: Runnable? = null
    private val nodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()
    private var nodeJsInterpreter: NodeJsInterpreter? = null
    private var previousText = ""

    private var myLastRenderedHtml = ""
    private var mainEditor: Editor? = null

    fun setMainEditor(editor: Editor?) {
        mainEditor = editor

        nodeJsInterpreter = nodeJsInterpreterRef.resolve(project)
        if (nodeJsInterpreter == null) {
            Messages.showMessageDialog(
                htmlPanelWrapper,
                "Node.js not configured, preview is not available",
                CommonBundle.getErrorTitle(),
                Messages.getErrorIcon()
            )
        }
    }

    override fun getPreferredFocusedComponent(): JComponent? = panel?.component

    override fun selectNotify() {
        if (panel != null) {
            updateHtmlPooled()
        }
    }

    override fun getComponent(): JComponent = htmlPanelWrapper
    override fun getName(): String = "MJML Preview Editor"
    override fun setState(state: FileEditorState) {}
    override fun isModified(): Boolean = false
    override fun isValid(): Boolean = true
    override fun deselectNotify() {}
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? = null
    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun dispose() {
        if (panel != null) {
            Disposer.dispose(panel!!)
        }
    }

    private fun retrievePanelProvider(): MjmlHtmlPanelProvider {
        val provider = JCEFHtmlPanelProvider()
        if (provider.isAvailable() !== MjmlHtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
            Messages.showMessageDialog(
                htmlPanelWrapper,
                "Failed to load mjml preview, please make sure you have jcef support enabled",
                CommonBundle.getErrorTitle(),
                Messages.getErrorIcon()
            )
        }

        return JCEFHtmlPanelProvider()
    }

    private fun renderWithNode(text: String): String {
        Files.writeString(tempFile.toPath(), text, StandardCharsets.UTF_8)
        nodeJsInterpreter ?: return "<p>Node not configured</p>"
        val line = AtomicInteger(0)
        val commandLineConfigurator = NodeCommandLineConfigurator.find(nodeJsInterpreter!!)
        val commandLine = GeneralCommandLine("node", FilePluginUtil.getFile("renderer/index.js").absolutePath)
            .withInput(tempFile)
            .withWorkDirectory(File(virtualFile.path).parentFile)

        commandLineConfigurator.configure(commandLine)

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
        buffer.append("\n")
        buffer.append("Last call")
        buffer.append(LocalDateTime.now())

        val mapper = jacksonObjectMapper()
        val renderResult: MjmlRenderResult

        try {
            renderResult = mapper.readValue(buffer.toString(), MjmlRenderResult::class.java)
        } catch (e: Exception) {
            return "failed to render"
        }

        val errors = renderResult.errors
            .filter { it.formattedMessage != null }
        if (!errors.isEmpty()) {
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
                    "<html><strong>Errors while rendering MJML</strong></html>",
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

        return renderResult.html ?: ""
    }

    // Is always run from pooled thread
    private fun updateHtml() {
        if (panel == null || document == null || !virtualFile.isValid || Disposer.isDisposed(this) || mainEditor == null || virtualFile == null) {
            return
        }
        val currentText = mainEditor!!.document.text
        if (myLastRenderedHtml != "" && currentText == previousText) {
            return
        }

        previousText = currentText
        val html = renderWithNode(currentText)

        synchronized(REQUESTS_LOCK) {
            if (lastHtmlOrRefreshRequest != null) {
                swingAlarm.cancelRequest(lastHtmlOrRefreshRequest!!)
            }

            lastHtmlOrRefreshRequest = Runnable {
                if (panel == null) return@Runnable
                val currentHtml = "<html><head></head>$html</html>"
                if (currentHtml != myLastRenderedHtml) {
                    myLastRenderedHtml = currentHtml
                    panel!!.setHtml(myLastRenderedHtml)
                }
                synchronized(REQUESTS_LOCK) { lastHtmlOrRefreshRequest = null }
            }

            swingAlarm.addRequest(
                lastHtmlOrRefreshRequest!!,
                RENDERING_DELAY_MS,
                ModalityState.stateForComponent(component)
            )
        }
    }

    private fun detachHtmlPanel() {
        if (panel != null) {
            htmlPanelWrapper.remove(panel!!.component)
            Disposer.dispose(panel!!)
            panel = null
        }
    }

    private fun attachHtmlPanel() {
        panel = retrievePanelProvider().createHtmlPanel()

        htmlPanelWrapper.add(panel!!.component, BorderLayout.CENTER)
        htmlPanelWrapper.repaint()
        myLastRenderedHtml = ""
        updateHtmlPooled()
    }

    private fun updateHtmlPooled() {
        pooledAlarm.cancelAllRequests()
        pooledAlarm.addRequest({ updateHtml() }, 0)
    }

    companion object {
        private const val PARSING_CALL_TIMEOUT_MS = 50L
        private const val RENDERING_DELAY_MS = 40L
        private fun isPreviewShown(project: Project, file: VirtualFile): Boolean {
            val state = EditorHistoryManager.getInstance(project).getState(file, MjmlPreviewFileEditorProvider())
            return if (state !is SplitFileEditor.MyFileEditorState) {
                true
            } else
                SplitFileEditor.SplitEditorLayout.valueOf(state.splitLayout!!) !=
                        SplitFileEditor.SplitEditorLayout.FIRST
        }
    }

    init {
        document?.addDocumentListener(object : DocumentListener {
            override fun beforeDocumentChange(e: DocumentEvent) {
                pooledAlarm.cancelAllRequests()
            }

            override fun documentChanged(e: DocumentEvent) {
                pooledAlarm.addRequest({ updateHtml() }, PARSING_CALL_TIMEOUT_MS)
            }
        }, this)
        htmlPanelWrapper = JPanel(BorderLayout())
        htmlPanelWrapper.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent) {
                swingAlarm.addRequest({
                    if (panel == null) {
                        attachHtmlPanel()
                    }
                }, 0, ModalityState.stateForComponent(component))
            }

            override fun componentHidden(e: ComponentEvent) {
                swingAlarm.addRequest({
                    if (panel != null) {
                        detachHtmlPanel()
                    }
                }, 0, ModalityState.stateForComponent(component))
            }
        })
        if (isPreviewShown(project, virtualFile)) {
            attachHtmlPanel()
        }
    }
}
