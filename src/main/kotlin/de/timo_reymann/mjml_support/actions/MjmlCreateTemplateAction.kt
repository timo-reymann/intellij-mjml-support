package de.timo_reymann.mjml_support.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import de.timo_reymann.mjml_support.icons.MjmlIcons

class MjmlCreateTemplateAction : CreateFileFromTemplateAction(NAME, DESCRIPTION, MjmlIcons.COLORED) {
    companion object {
        private const val TEMPLATE_NAME: String = "MJML File"
        private const val NAME = "MJML File"
        private const val DESCRIPTION = "Creates MJML file"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New $NAME")
            .addKind(NAME, MjmlIcons.COLORED, TEMPLATE_NAME)
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
        "Create $NAME $newName"

    override fun createFile(name: String, templateName: String?, dir: PsiDirectory?): PsiFile? {
        val template = CustomFileTemplate(name, "mjml")
        return createFileFromTemplate(name, template, dir)
    }
}
