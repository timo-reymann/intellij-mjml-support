package de.timo_reymann.mjml_support.editor

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor

class MjmlPreviewFileEditorProvider : WeighedFileEditorProvider() {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        if (!MarkdownHtmlPanelProvider.hasAvailableProviders()) {
            return false
        }

        val fileType = file.fileType

        return fileType === MjmlHtmlFileType.INSTANCE ||
                ScratchUtil.isScratch(file) &&
                LanguageUtil.getLanguageForPsi(project, file, fileType) === MjmlHtmlLanguage.INSTANCE
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        // TODO Implement own
        return MarkdownPreviewFileEditor(project, file)
    }

    override fun getEditorTypeId(): String {
        return "mjml-preview-editor"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
    }
}