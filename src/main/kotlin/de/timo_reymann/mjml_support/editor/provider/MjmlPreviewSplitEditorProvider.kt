package de.timo_reymann.mjml_support.editor.provider

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider
import de.timo_reymann.mjml_support.editor.ui.MjmlPreviewFileEditor
import de.timo_reymann.mjml_support.editor.ui.MjmlSplitEditor

class MjmlPreviewSplitEditorProvider :
    SplitTextEditorProvider(PsiAwareTextEditorProvider(), MjmlPreviewFileEditorProvider()) {

    override fun createSplitEditor(firstEditor: FileEditor, secondEditor: FileEditor): FileEditor {
        require(!(firstEditor !is TextEditor || secondEditor !is MjmlPreviewFileEditor)) { "Main editor should be TextEditor" }
        return MjmlSplitEditor(
            firstEditor,
            secondEditor
        )
    }

}
