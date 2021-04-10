package de.timo_reymann.mjml_support.editor

import com.intellij.CommonBundle
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBSplitter
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.intellij.util.Alarm
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.provider.JCEFHtmlPanelProvider
import de.timo_reymann.mjml_support.editor.provider.MjmlPreviewFileEditorProvider
import de.timo_reymann.mjml_support.editor.render.MjmlRenderer
import de.timo_reymann.mjml_support.settings.MJML_SETTINGS_CHANGED_TOPIC
import de.timo_reymann.mjml_support.settings.MjmlSettings
import de.timo_reymann.mjml_support.settings.MjmlSettingsChangedListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeListener
import java.lang.Exception
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities


class MjmlPreviewFileEditor(private val project: Project, private val virtualFile: VirtualFile) :
    UserDataHolderBase(), FileEditor, MjmlSettingsChangedListener {
    private val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

    private val htmlPanelWrapper: JPanel
    private var panel: JCEFHtmlPanel? = null
    private var mainEditor: Editor? = null
    internal var previewWidthStatus: PreviewWidthStatus? = null

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

    fun setPreviewWidth(previewWidthStatus: PreviewWidthStatus) {
        this.previewWidthStatus = previewWidthStatus
        this.updatePreviewWidth()
    }

    private fun updatePreviewWidth() {
        (htmlPanelWrapper.parent as JBSplitter?)?.let {
            it.proportion = 1f
            it.setResizeEnabled(false)
        }

        val comp = getPanel()?.component ?: return
        val size = Dimension(previewWidthStatus!!.width + 5, comp.size.height)
        comp.size = size
        comp.preferredSize = size

        htmlPanelWrapper.minimumSize = size
        htmlPanelWrapper.preferredSize = size
    }

    override fun getPreferredFocusedComponent(): JComponent? = panel?.component

    fun getPanel(): JCEFHtmlPanel? = this.panel

    override fun selectNotify() {
        if (panel != null) {
            updateHtmlPooled()
        }
    }

    override fun getComponent(): JComponent = htmlPanelWrapper
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
    private fun updateHtml(force: Boolean) {
        if (panel == null || document == null || !virtualFile.isValid || Disposer.isDisposed(this) || mainEditor == null) {
            return
        }

        val currentText = mainEditor!!.document.text
        if (!force && myLastRenderedHtml != "" && currentText == previousText) {
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
                if (force || currentHtml != myLastRenderedHtml) {
                    myLastRenderedHtml = currentHtml
                    panel!!.setHtml(myLastRenderedHtml)
                }
                synchronized(REQUESTS_LOCK) { lastHtmlOrRefreshRequest = null }
            }

            if (!swingAlarm.isDisposed) {
                swingAlarm.addRequest(
                    lastHtmlOrRefreshRequest!!,
                    RENDERING_DELAY_MS,
                    ModalityState.stateForComponent(component)
                )
            }
        }
    }

    private fun detachHtmlPanel() {
        if (panel == null) {
            return
        }

        previewWidthStatus = DEFAULT_PREVIEW_WIDTH
        htmlPanelWrapper.remove(panel!!.component)
        Disposer.dispose(panel!!)
        panel = null
    }

    private fun attachHtmlPanel() {
        previewWidthStatus = DEFAULT_PREVIEW_WIDTH
        panel = retrievePanelProvider().createHtmlPanel()
        myLastRenderedHtml = ""

        val c = GridBagConstraints()
        c.fill = GridBagConstraints.VERTICAL
        c.weightx = 0.0
        c.weighty = 1.0
        c.anchor = GridBagConstraints.EAST
        htmlPanelWrapper.add(panel!!.component, c)

        updatePreviewWidth()
        htmlPanelWrapper.repaint()
        updateHtmlPooled()
    }

    private fun updateHtmlPooled(force: Boolean = false) {
        pooledAlarm.cancelAllRequests()
        pooledAlarm.addRequest({ updateHtml(force) }, 0)
    }

    companion object {
        private val DEFAULT_PREVIEW_WIDTH = PreviewWidthStatus.DESKTOP
        private const val PARSING_CALL_TIMEOUT_MS = 50L
        private const val RENDERING_DELAY_MS = 40L
        private fun isPreviewShown(project: Project, file: VirtualFile): Boolean {
            val editorState = EditorHistoryManager.getInstance(project).getState(file, MjmlPreviewFileEditorProvider())
            return when (editorState) {
                !is MyFileEditorState -> true
                else -> SplitEditorLayout.valueOf(editorState.splitLayout!!) != SplitEditorLayout.FIRST
            }
        }
    }

    init {
        document?.addDocumentListener(object : DocumentListener {
            override fun beforeDocumentChange(e: DocumentEvent) {
                pooledAlarm.cancelAllRequests()
            }

            override fun documentChanged(e: DocumentEvent) {
                pooledAlarm.addRequest({ updateHtml(false) }, PARSING_CALL_TIMEOUT_MS)
            }
        }, this)
        htmlPanelWrapper = JPanel(GridBagLayout())

        htmlPanelWrapper.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent) = swingAlarm.addRequest({
                if (panel == null) {
                    attachHtmlPanel()
                }
            }, 10, ModalityState.stateForComponent(component))

            override fun componentHidden(e: ComponentEvent) = swingAlarm.addRequest({
                if (panel != null) {
                    detachHtmlPanel()
                }
            }, 10, ModalityState.stateForComponent(component))
        })

        if (isPreviewShown(project, virtualFile)) {
            GlobalScope.launch {
                attachHtmlPanel()
            }
        }

        ApplicationManager.getApplication().messageBus.connect(this)
            .subscribe(MJML_SETTINGS_CHANGED_TOPIC, this)
    }

    override fun onChanged(settings: MjmlSettings) {
        updateHtmlPooled(true)
    }
}
