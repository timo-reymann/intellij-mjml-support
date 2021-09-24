package de.timo_reymann.mjml_support.editor.provider

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import de.timo_reymann.mjml_support.editor.ui.MjmlPreviewFileEditor
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage

class MjmlPreviewFileEditorProvider : WeighedFileEditorProvider() {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        val fileType = file.fileType

        return fileType === MjmlHtmlFileType.INSTANCE ||
                ScratchUtil.isScratch(file) &&
                LanguageUtil.getLanguageForPsi(project, file) === MjmlHtmlLanguage.INSTANCE
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = MjmlPreviewFileEditor(project, file)

    override fun getEditorTypeId(): String = "mjml-preview-editor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}
