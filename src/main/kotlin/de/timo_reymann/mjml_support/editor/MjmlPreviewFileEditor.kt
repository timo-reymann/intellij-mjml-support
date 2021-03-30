package de.timo_reymann.mjml_support.editor

import com.intellij.CommonBundle
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.intellij.util.Alarm
import org.intellij.plugins.markdown.ui.split.SplitFileEditor
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeListener
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JComponent
import javax.swing.JPanel


class MjmlPreviewFileEditor(private val myProject: Project, private val myFile: VirtualFile) :
    UserDataHolderBase(), FileEditor {
    private val myDocument: Document? = FileDocumentManager.getInstance().getDocument(myFile)
    private val myHtmlPanelWrapper: JPanel
    private var myPanel: JCEFHtmlPanel? = null
    private val myPooledAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val mySwingAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private val REQUESTS_LOCK = Any()
    private var myLastScrollRequest: Runnable? = null
    private var myLastHtmlOrRefreshRequest: Runnable? = null
    private val nodeJsInterpreterRef = NodeJsInterpreterRef.createProjectRef()
    private var nodeJsInterpreter: NodeJsInterpreter? = null

    @Volatile
    private var myLastScrollOffset = 0
    private var myLastRenderedHtml = ""
    private var mainEditor: Editor? = null

    fun setMainEditor(editor: Editor?) {
        mainEditor = editor
        nodeJsInterpreter = nodeJsInterpreterRef.resolve(mainEditor!!.project!!)
        // TODO handle
        if (nodeJsInterpreter == null) {
            Messages.showMessageDialog(
                myHtmlPanelWrapper,
                "Node.js not configured",
                CommonBundle.getErrorTitle(),
                Messages.getErrorIcon()
            )
        }

    }

    fun scrollToSrcOffset(offset: Int) {
        if (myPanel == null) return

        // Do not scroll if html update request is online
        // This will restrain preview from glitches on editing
        if (!myPooledAlarm.isEmpty) {
            myLastScrollOffset = offset
            return
        }
        synchronized(REQUESTS_LOCK) {
            if (myLastScrollRequest != null) {
                mySwingAlarm.cancelRequest(myLastScrollRequest!!)
            }
            myLastScrollRequest = Runnable {
                if (myPanel != null) {
                    myLastScrollOffset = offset
                    synchronized(REQUESTS_LOCK) { myLastScrollRequest = null }
                }
            }
            mySwingAlarm.addRequest(
                myLastScrollRequest!!,
                RENDERING_DELAY_MS,
                ModalityState.stateForComponent(component)
            )
        }
    }


    override fun getPreferredFocusedComponent(): JComponent? = myPanel?.component

    override fun selectNotify() {
        if (myPanel != null) {
            updateHtmlPooled()
        }
    }

    override fun getComponent(): JComponent = myHtmlPanelWrapper
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
        if (myPanel != null) {
            Disposer.dispose(myPanel!!)
        }
    }

    private fun retrievePanelProvider(): MjmlHtmlPanelProvider {
        val provider = JCEFHtmlPanelProvider()
        if (provider.isAvailable() !== MjmlHtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
            Messages.showMessageDialog(
                myHtmlPanelWrapper,
                "Failed to load mjml preview, please make sure you have jcef support enabled",
                CommonBundle.getErrorTitle(),
                Messages.getErrorIcon()
            )
        }


        return JCEFHtmlPanelProvider()
    }

    private fun renderWithNode(text: String): String {
        nodeJsInterpreter ?: return "<p>Node not configured</p>"
        val line = AtomicInteger(0)
        val commandLineConfigurator = NodeCommandLineConfigurator.find(nodeJsInterpreter!!)
        val commandLine = GeneralCommandLine("node", "--version")
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
        return buffer.toString()
    }

    // Is always run from pooled thread
    private fun updateHtml() {
        if (myPanel == null || myDocument == null || !myFile.isValid || Disposer.isDisposed(this) || mainEditor == null) {
            return
        }

        val html = renderWithNode(mainEditor!!.document.text)

        synchronized(REQUESTS_LOCK) {
            if (myLastHtmlOrRefreshRequest != null) {
                mySwingAlarm.cancelRequest(myLastHtmlOrRefreshRequest!!)
            }

            myLastHtmlOrRefreshRequest = Runnable {
                if (myPanel == null) return@Runnable
                val currentHtml = "<html><head></head>$html</html>"
                if (currentHtml != myLastRenderedHtml) {
                    myLastRenderedHtml = currentHtml
                    myPanel!!.setHtml(myLastRenderedHtml)
                }
                synchronized(REQUESTS_LOCK) { myLastHtmlOrRefreshRequest = null }
            }

            mySwingAlarm.addRequest(
                myLastHtmlOrRefreshRequest!!,
                RENDERING_DELAY_MS,
                ModalityState.stateForComponent(component)
            )
        }
    }

    private fun detachHtmlPanel() {
        if (myPanel != null) {
            myHtmlPanelWrapper.remove(myPanel!!.component)
            Disposer.dispose(myPanel!!)
            myPanel = null
        }
    }

    private fun attachHtmlPanel() {
        myPanel = retrievePanelProvider().createHtmlPanel()

        myHtmlPanelWrapper.add(myPanel!!.component, BorderLayout.CENTER)
        myHtmlPanelWrapper.repaint()
        myLastRenderedHtml = ""
        updateHtmlPooled()
    }

    private fun updateHtmlPooled() {
        myPooledAlarm.cancelAllRequests()
        myPooledAlarm.addRequest({ updateHtml() }, 0)
    }

    companion object {
        private const val PARSING_CALL_TIMEOUT_MS = 50L
        private const val RENDERING_DELAY_MS = 20L
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
        myDocument?.addDocumentListener(object : DocumentListener {
            override fun beforeDocumentChange(e: DocumentEvent) {
                myPooledAlarm.cancelAllRequests()
            }

            override fun documentChanged(e: DocumentEvent) {
                myPooledAlarm.addRequest({ updateHtml() }, PARSING_CALL_TIMEOUT_MS)
            }
        }, this)
        myHtmlPanelWrapper = JPanel(BorderLayout())
        myHtmlPanelWrapper.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent) {
                mySwingAlarm.addRequest({
                    if (myPanel == null) {
                        attachHtmlPanel()
                    }
                }, 0, ModalityState.stateForComponent(component))
            }

            override fun componentHidden(e: ComponentEvent) {
                mySwingAlarm.addRequest({
                    if (myPanel != null) {
                        detachHtmlPanel()
                    }
                }, 0, ModalityState.stateForComponent(component))
            }
        })
        if (isPreviewShown(myProject, myFile)) {
            attachHtmlPanel()
        }
    }
}
