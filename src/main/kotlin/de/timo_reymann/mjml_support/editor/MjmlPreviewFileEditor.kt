package de.timo_reymann.mjml_support.editor

import com.intellij.CommonBundle
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
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
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.intellij.util.Alarm
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.render.MjmlRenderer
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeListener
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel

import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.BorderFactory


class MjmlPreviewFileEditor(private val project: Project, private val virtualFile: VirtualFile) :
    UserDataHolderBase(), FileEditor {
    private val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

    private val htmlPanelWrapper: JPanel
    private var panel: JCEFHtmlPanel? = null
    private var mainEditor: Editor? = null

    private val pooledAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val swingAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private var lastHtmlOrRefreshRequest: Runnable? = null
    private val REQUESTS_LOCK = Any()

    private val mjmlRenderer = MjmlRenderer(project, virtualFile)

    private var previousText = ""
    private var myLastRenderedHtml = ""

    fun setMainEditor(editor: Editor?) {
        mainEditor = editor
    }

    override fun getPreferredFocusedComponent(): JComponent? = panel?.component

    fun getPanel(): JCEFHtmlPanel? {
        return this.panel
    }

    override fun selectNotify() {
        if (panel != null) {
            updateHtmlPooled()
        }
    }

    override fun getComponent(): JComponent {
        return htmlPanelWrapper
    }
    override fun getName(): String = MjmlBundle.message("mjml_preview.name")
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
                MjmlBundle.message("mjml_preview.jcef_disabled"),
                CommonBundle.getErrorTitle(),
                Messages.getErrorIcon()
            )
        }

        return JCEFHtmlPanelProvider()
    }

    // Is always run from pooled thread
    private fun updateHtml() {
        if (panel == null || document == null || !virtualFile.isValid || Disposer.isDisposed(this) || mainEditor == null) {
            return
        }

        val currentText = mainEditor!!.document.text
        if (myLastRenderedHtml != "" && currentText == previousText) {
            return
        }

        previousText = currentText
        val html = mjmlRenderer.render(currentText)

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
        val c = GridBagConstraints()
        c.fill = GridBagConstraints.VERTICAL;
        c.weightx = 0.0;
        c.weighty = 1.0;
        htmlPanelWrapper.add(panel!!.component, c)
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
            return if (state !is MyFileEditorState) {
                true
            } else
                SplitEditorLayout.valueOf(state.splitLayout!!) !=
                        SplitEditorLayout.FIRST
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
        htmlPanelWrapper = JPanel(GridBagLayout())
        htmlPanelWrapper.minimumSize = Dimension(800,0)
        htmlPanelWrapper.preferredSize = Dimension(800,0)

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
