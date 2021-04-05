package de.timo_reymann.mjml_support.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import javax.swing.Icon

class MjmlSplitEditor(val mainEditor: TextEditor, val secondEditor: MjmlPreviewFileEditor) :
    TextEditorWithPreview(mainEditor, secondEditor, "TextEditorWithPreview", Layout.SHOW_EDITOR),
    TextEditor {
    protected var previewWidthStatus = PreviewWidthStatus.TABLET

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
        PreviewWidthChangeAction(PreviewWidthStatus.TABLET),
        PreviewWidthChangeAction(PreviewWidthStatus.MOBILE)
    )

    init {
        secondEditor.setMainEditor(mainEditor.editor)
    }

    inner class PreviewWidthChangeAction(private val myPreviewWidthStatus: PreviewWidthStatus) :
        ToggleAction(myPreviewWidthStatus.text, myPreviewWidthStatus.description, myPreviewWidthStatus.icon),
        DumbAware {

        override fun isSelected(e: AnActionEvent): Boolean = myPreviewWidthStatus == previewWidthStatus

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            if (!state) {
                return
            }

            // TODO Change preview width / trigger rerender
            previewWidthStatus = myPreviewWidthStatus
        }
    }
}

enum class PreviewWidthStatus(val text: String, val description: String, val icon: Icon) {
    // TODO Set right icons
    MOBILE("Mobile Preview", "Show preview for mobile devices", AllIcons.Xml.Browsers.Firefox),
    TABLET("Tablet Preview", "Show preview for table devices", AllIcons.Xml.Browsers.Chromium),
    DESKTOP("Desktop Preview", "Show desktop preview", AllIcons.Xml.Browsers.Edge);
}


