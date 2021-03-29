package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.VisibleAreaEvent
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.openapi.editor.ex.util.EditorUtil
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

    private inner class MyVisibleAreaListener : VisibleAreaListener {
        private var previousLine = 0
        override fun visibleAreaChanged(event: VisibleAreaEvent) {
            val editor = event.editor
            val currentLine = EditorUtil.yPositionToLogicalLine(editor, editor.scrollingModel.verticalScrollOffset)
            if (currentLine == previousLine) {
                return
            }
            previousLine = currentLine
            secondEditor.scrollToSrcOffset(EditorUtil.getVisualLineEndOffset(editor, currentLine))
        }
    }

    init {
        secondEditor.setMainEditor(mainEditor.editor)
        mainEditor.editor.scrollingModel.addVisibleAreaListener(MyVisibleAreaListener())
    }
}
