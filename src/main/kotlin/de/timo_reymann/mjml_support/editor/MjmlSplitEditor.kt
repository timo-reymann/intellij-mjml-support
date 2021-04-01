package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import org.intellij.plugins.markdown.ui.split.SplitFileEditor

class MjmlSplitEditor(mainEditor: TextEditor, secondEditor: MjmlPreviewFileEditor) :
    SplitFileEditor<TextEditor?, MjmlPreviewFileEditor?>(mainEditor, secondEditor),
    TextEditor {

    override fun getName(): String = "MJML Split Editor"

    override fun getFile(): VirtualFile? = mainEditor.file

    override fun getEditor(): Editor = mainEditor.editor

    override fun canNavigateTo(navigatable: Navigatable): Boolean = mainEditor.canNavigateTo(navigatable)

    override fun navigateTo(navigatable: Navigatable) = mainEditor.navigateTo(navigatable)

    init {
        secondEditor.setMainEditor(mainEditor.editor)
    }
}
