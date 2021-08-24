package de.timo_reymann.mjml_support.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.icons.EditorIcons
import de.timo_reymann.mjml_support.mock.AnActionEventMock
import javax.swing.Icon

open class MjmlSplitEditor(private val mainEditor: TextEditor, val secondEditor: MjmlPreviewFileEditor) :
    TextEditorWithPreview(mainEditor, secondEditor, "TextEditorWithPreview", Layout.SHOW_EDITOR),
    TextEditor {

    override fun getName(): String = MjmlBundle.message("mjml_preview.name")

    override fun getFile(): VirtualFile? = mainEditor.file

    override fun getEditor(): Editor = mainEditor.editor

    override fun canNavigateTo(navigatable: Navigatable): Boolean = mainEditor.canNavigateTo(navigatable)

    override fun navigateTo(navigatable: Navigatable) = mainEditor.navigateTo(navigatable)

    override fun setState(state: FileEditorState) {
        if (state is MjmlFileEditorState) {
            if (state.firstState != null) {
                myEditor.setState(state.firstState)
            }

            if (state.secondState != null) {
                myPreview.setState(state.secondState)
            }

            // Trigger manual click, this is necessary because we don't have access to the underlying layout directly
            val event = AnActionEventMock()
            when (state.splitLayout) {
                Layout.SHOW_EDITOR -> showEditorAction.setSelected(event, true)
                Layout.SHOW_PREVIEW -> showPreviewAction.setSelected(event, true)
                else -> showEditorAndPreviewAction.setSelected(event, true)
            }
        }
    }

    override fun getState(level: FileEditorStateLevel): FileEditorState {
        return MjmlFileEditorState(super.getLayout(), myEditor.getState(level), myPreview.getState(level))
    }

    override fun createViewActionGroup(): ActionGroup = DefaultActionGroup(
        showEditorAction,
        showEditorAndPreviewAction,
        showPreviewAction,
        Separator.create(),
        object : AnAction("Refresh", "Refresh the preview", AllIcons.Actions.Refresh) {
            override fun actionPerformed(e: AnActionEvent) {
                secondEditor.forceRerender()
            }
        },
        Separator.create(),
        object : ToggleAction("Keep Scroll Position", "Keep scroll position on rerendering",AllIcons.Actions.SynchronizeScrolling) {
            override fun isSelected(e: AnActionEvent): Boolean = secondEditor.isScrollSync()

            override fun setSelected(e: AnActionEvent, state: Boolean) {
                secondEditor.setScrollSync(state)
            }
        },
        object : ToggleAction("Show HTML", "", AllIcons.FileTypes.Html) {
            override fun isSelected(e: AnActionEvent): Boolean = secondEditor.isHtmlPreview()

            override fun setSelected(e: AnActionEvent, show: Boolean) {
                if (show) {
                    secondEditor.createSourceViewer()
                } else {
                    secondEditor.removeSourceViewer()
                }
            }
        },
        PreviewWidthChangeAction(PreviewWidthStatus.DESKTOP),
        PreviewWidthChangeAction(PreviewWidthStatus.MOBILE)
    )

    init {
        secondEditor.setMainEditor(mainEditor.editor)
    }

    inner class PreviewWidthChangeAction(private val myPreviewWidthStatus: PreviewWidthStatus) :
        ToggleAction(myPreviewWidthStatus.text, myPreviewWidthStatus.description, myPreviewWidthStatus.icon),
        DumbAware {

        override fun isSelected(e: AnActionEvent): Boolean = myPreviewWidthStatus == secondEditor.previewWidthStatus
                && !secondEditor.isHtmlPreview()
                && layout != Layout.SHOW_PREVIEW

        private fun select() = secondEditor.setPreviewWidth(myPreviewWidthStatus)

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            if (state) {
                select()
            }
        }
    }
}

enum class PreviewWidthStatus(val text: String, val description: String, val width: Int, val icon: Icon) {
    MOBILE("Mobile Preview", "Show preview for mobile devices", 400, EditorIcons.SMARTPHONE),
    DESKTOP("Desktop Preview", "Show desktop preview", 800, EditorIcons.DESKTOP);
}
