package de.timo_reymann.mjml_support.editor

import com.intellij.CommonBundle
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
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
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBSplitter
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
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
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel


class MjmlPreviewFileEditor(private val project: Project, private val virtualFile: VirtualFile) :
    UserDataHolderBase(), FileEditor, MjmlSettingsChangedListener {
    private val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

    private val htmlPanelWrapper: JPanel
    private var htmlPanel: JCEFHtmlPanel? = null
    private var mainEditor: Editor? = null
    internal var previewWidthStatus: PreviewWidthStatus? = null
    internal var sourceViewer: EditorTextField? = null

    private val pooledAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val swingAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private var lastHtmlOrRefreshRequest: Runnable? = null
    private val requestsLock = Any()

    private val mjmlRenderer = MjmlRenderer(project, virtualFile)

    private var previousText = ""
    private var myLastRenderedHtml = ""

    fun setMainEditor(editor: Editor?) {
        mainEditor = editor
    }

    fun setPreviewWidth(previewWidthStatus: PreviewWidthStatus) {
        // Cleanup html viewer before updating width
        if (isHtmlPreview()) {
            removeSourceViewer()
        }

        this.previewWidthStatus = previewWidthStatus
        this.updatePreviewWidth(previewWidthStatus.width)
    }

    private fun updatePreviewWidth(width: Int, resizable: Boolean = false) {
        (htmlPanelWrapper.parent as JBSplitter?)?.let {
            it.proportion = 1f
            it.setResizeEnabled(resizable)
            it.baselineResizeBehavior
        }
        val size = Dimension(width + 5, 1)

        // Set panel wrapper width, minimum size is used by splitter
        htmlPanelWrapper.minimumSize = size

        htmlPanel!!.component.size = size
    }

    override fun getPreferredFocusedComponent(): JComponent? = htmlPanel?.component

    private fun getPanel(): JCEFHtmlPanel? = this.htmlPanel

    override fun selectNotify() {
        if (htmlPanel != null) {
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
        if (htmlPanel != null) {
            Disposer.dispose(htmlPanel!!)
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
        if (htmlPanel == null || document == null || !virtualFile.isValid || Disposer.isDisposed(this) || mainEditor == null) {
            return
        }

        val currentText = mainEditor!!.document.text
        if (!force && myLastRenderedHtml != "" && currentText == previousText) {
            return
        }

        previousText = currentText
        val html = mjmlRenderer.render(currentText)

        synchronized(requestsLock) {
            if (lastHtmlOrRefreshRequest != null) {
                swingAlarm.cancelRequest(lastHtmlOrRefreshRequest!!)
            }

            lastHtmlOrRefreshRequest = Runnable {
                if (htmlPanel == null) return@Runnable
                val currentHtml = "<html><head></head>$html</html>"
                if (force || currentHtml != myLastRenderedHtml) {
                    myLastRenderedHtml = currentHtml
                    setComponentHtml()
                }
                synchronized(requestsLock) { lastHtmlOrRefreshRequest = null }
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

    private fun setComponentHtml() {
        if (isHtmlPreview()) {
            WriteAction.run<Exception> {
                sourceViewer!!.document.setText(myLastRenderedHtml)
            }
        } else if (htmlPanel != null) {
            htmlPanel!!.setHtml(myLastRenderedHtml)
        }
    }

    private fun detachHtmlPanel() {
        if (htmlPanel == null) {
            return
        }

        previewWidthStatus = DEFAULT_PREVIEW_WIDTH
        htmlPanelWrapper.removeAll()
        Disposer.dispose(htmlPanel!!)
        htmlPanel = null
    }

    fun isHtmlPreview(): Boolean {
        return sourceViewer != null
    }

    fun createSourceViewer() {
        // Make html source resizable
        updatePreviewWidth(800, true)

        htmlPanelWrapper.removeAll()
        val document = EditorFactory.getInstance().createDocument(myLastRenderedHtml)

        // Create source viewer with html snippet
        sourceViewer = EditorTextField(document, project, HtmlFileType.INSTANCE, true, false)

        val scrollPane = JBScrollPane(sourceViewer)
        htmlPanelWrapper.add(scrollPane, BROWSER_PANEL_CONSTRAINTS)
    }

    fun removeSourceViewer() {
        htmlPanelWrapper.removeAll()
        attachHtmlPanel()
        sourceViewer = null
    }

    private fun attachHtmlPanel() {
        previewWidthStatus = DEFAULT_PREVIEW_WIDTH
        htmlPanel = retrievePanelProvider().createHtmlPanel()
        myLastRenderedHtml = ""

        htmlPanelWrapper.add(htmlPanel!!.component, BROWSER_PANEL_CONSTRAINTS)

        updatePreviewWidth(previewWidthStatus!!.width)
        htmlPanelWrapper.repaint()
        updateHtmlPooled()
    }

    fun forceRerender() {
        updateHtmlPooled(true)
    }

    private fun updateHtmlPooled(force: Boolean = false) {
        pooledAlarm.cancelAllRequests()
        pooledAlarm.addRequest({ updateHtml(force) }, 0)
    }

    companion object {
        private val DEFAULT_PREVIEW_WIDTH = PreviewWidthStatus.DESKTOP
        private const val PARSING_CALL_TIMEOUT_MS = 50L
        private const val RENDERING_DELAY_MS = 40L
        private val BROWSER_PANEL_CONSTRAINTS = GridBagConstraints()

        private fun isPreviewShown(project: Project, file: VirtualFile): Boolean {
            val editorState = EditorHistoryManager.getInstance(project).getState(file, MjmlPreviewFileEditorProvider())
            return when (editorState) {
                !is MyFileEditorState -> true
                else -> SplitEditorLayout.valueOf(editorState.splitLayout!!) != SplitEditorLayout.FIRST
            }
        }

        init {
            BROWSER_PANEL_CONSTRAINTS.fill = GridBagConstraints.BOTH
            BROWSER_PANEL_CONSTRAINTS.weightx = 1.0
            BROWSER_PANEL_CONSTRAINTS.weighty = 1.0
            BROWSER_PANEL_CONSTRAINTS.gridwidth = GridBagConstraints.HORIZONTAL
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
                if (htmlPanel == null) {
                    attachHtmlPanel()
                }
            }, 10, ModalityState.stateForComponent(component))

            override fun componentHidden(e: ComponentEvent) = swingAlarm.addRequest({
                if (htmlPanel != null) {
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
        forceRerender()
    }
}
