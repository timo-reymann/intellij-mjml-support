package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider

class MjmlPreviewSplitEditorProvider : SplitTextEditorProvider(PsiAwareTextEditorProvider(), MjmlPreviewFileEditorProvider()) {

    override fun createSplitEditor(firstEditor: FileEditor, secondEditor: FileEditor): FileEditor {
        require(!(firstEditor !is TextEditor || secondEditor !is MjmlPreviewFileEditor)) { "Main editor should be TextEditor" }
        return MjmlSplitEditor(
            firstEditor,
            secondEditor
        )
    }

}
