package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor
import org.intellij.plugins.markdown.ui.preview.MarkdownSplitEditor
import org.intellij.plugins.markdown.ui.split.SplitTextEditorProvider

class MjmlPreviewSplitEditorProvider :
    SplitTextEditorProvider(PsiAwareTextEditorProvider(), MjmlPreviewFileEditorProvider()) {

    override fun createSplitEditor(firstEditor: FileEditor, secondEditor: FileEditor): FileEditor {
        require(!(firstEditor !is TextEditor || secondEditor !is MarkdownPreviewFileEditor)) { "Main editor should be TextEditor" }
        // TODO Implement own
        return MarkdownSplitEditor(
            firstEditor,
            secondEditor
        )
    }
}