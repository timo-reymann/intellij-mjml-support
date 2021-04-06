package de.timo_reymann.mjml_support.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.ui.JBSplitter
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.icons.EditorIcons
import java.awt.Dimension
import javax.swing.Icon

open class MjmlSplitEditor(val mainEditor: TextEditor, val secondEditor: MjmlPreviewFileEditor) :
    TextEditorWithPreview(mainEditor, secondEditor, "TextEditorWithPreview", Layout.SHOW_EDITOR),
    TextEditor {
    protected var previewWidthStatus = PreviewWidthStatus.DESKTOP

    override fun getName(): String = MjmlBundle.message("mjml_preview.name")

    override fun getFile(): VirtualFile? = mainEditor.file

    override fun getEditor(): Editor = mainEditor.editor

    override fun canNavigateTo(navigatable: Navigatable): Boolean = mainEditor.canNavigateTo(navigatable)

    override fun navigateTo(navigatable: Navigatable) = mainEditor.navigateTo(navigatable)

    override fun createViewActionGroup(): ActionGroup = DefaultActionGroup(
        showEditorAction,
        showEditorAndPreviewAction,
        showPreviewAction,
        Separator.create(),
        PreviewWidthChangeAction(PreviewWidthStatus.DESKTOP),
        PreviewWidthChangeAction(PreviewWidthStatus.MOBILE)
    )

    init {
        secondEditor.setMainEditor(mainEditor.editor)
        PreviewWidthChangeAction(previewWidthStatus).select()
    }

    inner class PreviewWidthChangeAction(private val myPreviewWidthStatus: PreviewWidthStatus) :
        ToggleAction(myPreviewWidthStatus.text, myPreviewWidthStatus.description, myPreviewWidthStatus.icon),
        DumbAware {

        override fun isSelected(e: AnActionEvent): Boolean = myPreviewWidthStatus == previewWidthStatus

        fun select() {
            // TODO Change preview width / trigger rerender
            val comp = secondEditor.getPanel()?.component ?: return
            comp.size = Dimension(myPreviewWidthStatus.width, comp.size.height)
            comp.preferredSize = Dimension(myPreviewWidthStatus.width, comp.preferredSize.height)
            previewWidthStatus = myPreviewWidthStatus
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            if (state) {
                select()
            }
        }
    }
}

enum class PreviewWidthStatus(val text: String, val description: String, val width: Int, val icon: Icon) {
    MOBILE("Mobile Preview", "Show preview for mobile devices", 320, EditorIcons.SMARTPHONE),
    DESKTOP("Desktop Preview", "Show desktop preview", 800, EditorIcons.DESKTOP);
}
