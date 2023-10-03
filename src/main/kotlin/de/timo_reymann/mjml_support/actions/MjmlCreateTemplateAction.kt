package de.timo_reymann.mjml_support.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.psi.PsiDirectory
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.icons.MjmlIcons
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import java.nio.file.Files
import java.nio.file.Path

class NewMjmlFileNameValidator(private val project: Project) : InputValidatorEx {

    override fun getErrorText(inputString: String): String? {
        if (inputString.trim().isEmpty()) {
            return MjmlBundle.message("create_action.error.empty.name")
        }

        if (Files.exists(Path.of(project.basePath, "$inputString.mjml"))) {
            return MjmlBundle.message("create_action.error.file.exists", inputString)
        }

        return null
    }

    override fun checkInput(inputString: String): Boolean = true

    override fun canClose(inputString: String): Boolean = getErrorText(inputString) == null
}

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
            .setValidator(NewMjmlFileNameValidator(project))
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
        MjmlBundle.message("create_action.create", NAME, newName)
}

class MjmlCreateFromTemplateHandler : DefaultCreateFromTemplateHandler() {
    override fun handlesTemplate(template: FileTemplate): Boolean =
        template.isTemplateOfType(MjmlHtmlFileType.INSTANCE)
}
