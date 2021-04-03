package de.timo_reymann.mjml_support.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.icons.MjmlIcons

class MjmlCreateTemplateAction : CreateFileFromTemplateAction(TEMPLATE_NAME, DESCRIPTION, MjmlIcons.COLORED) {
    companion object {
        private val TEMPLATE_NAME: String = MjmlBundle.message("create_action.template_name")
        private val NAME = MjmlBundle.message("create_action.name")
        private val DESCRIPTION = MjmlBundle.message("create_action.description")
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle(MjmlBundle.message("create_action.new", NAME))
            .addKind(NAME, MjmlIcons.COLORED, TEMPLATE_NAME)
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
        MjmlBundle.message("create_action.create", NAME, newName)

    override fun createFile(name: String, templateName: String?, dir: PsiDirectory?): PsiFile? {
        val template = CustomFileTemplate(name, "mjml")
        return createFileFromTemplate(name, template, dir)
    }
}
