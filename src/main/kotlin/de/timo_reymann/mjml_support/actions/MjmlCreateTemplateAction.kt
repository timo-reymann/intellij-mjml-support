package de.timo_reymann.mjml_support.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.icons.MjmlIcons
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

class MjmlCreateTemplateAction : CreateFileFromTemplateAction(TEMPLATE_NAME, DESCRIPTION, MjmlIcons.COLORED) {
    companion object {
        val TEMPLATE_NAME: String = MjmlBundle.message("create_action.template_name")
        val NAME = MjmlBundle.message("create_action.name")
        val DESCRIPTION = MjmlBundle.message("create_action.description")
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle(MjmlBundle.message("create_action.new", NAME))
            .addKind(NAME, MjmlIcons.COLORED, TEMPLATE_NAME)
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
        MjmlBundle.message("create_action.create", NAME, newName)
}

class MjmlCreateFromTemplateHandler : DefaultCreateFromTemplateHandler() {
    override fun handlesTemplate(template: FileTemplate): Boolean =
        template.isTemplateOfType(MjmlHtmlFileType.INSTANCE)
}
