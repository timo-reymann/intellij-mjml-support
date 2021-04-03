package de.timo_reymann.mjml_support.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.xml.XmlAttribute
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.model.MjmlAttributeType
import de.timo_reymann.mjml_support.model.getMjmlTagFromAttribute
import de.timo_reymann.mjml_support.util.MessageBusUtil
import java.io.IOException

class InvalidPathAttributeInspection : HtmlLocalInspectionTool() {
    override fun checkAttribute(attribute: XmlAttribute, holder: ProblemsHolder, isOnTheFly: Boolean) {
        val mjmlTag = getMjmlTagFromAttribute(attribute) ?: return
        val mjmlAttribute = mjmlTag.getAttributeByName(attribute.name) ?: return
        if (mjmlAttribute.type != MjmlAttributeType.PATH) {
            return
        }
        val filename = attribute.value

        if (filename == null) {
            holder.registerProblem(
                attribute,
                MjmlBundle.message("inspections.undefined_include"),
                ProblemHighlightType.ERROR,
            )
            return
        }

        val virtualFile = VfsUtilCore.findRelativeFile(filename, attribute.containingFile.virtualFile)
        if (virtualFile == null || !virtualFile.isValid || virtualFile.isDirectory || virtualFile.fileType != MjmlHtmlFileType.INSTANCE) {
            var fixes = arrayOf<LocalQuickFix>()

            if (wouldBeValidMjmlFile(filename)) {
                fixes = arrayOf(CreateMjmlFileQuickFix())
            }

            holder.registerProblem(
                attribute,
                MjmlBundle.message("inspections.invalid_mjml_file", attribute.value!!),
                ProblemHighlightType.ERROR,
                *fixes
            )
        }
    }

    private fun wouldBeValidMjmlFile(fileName: String): Boolean {
        return (fileName.endsWith(".${MjmlHtmlFileType.INSTANCE.defaultExtension}"))
    }
}

class CreateMjmlFileQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = "Create mjml file"
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val file = descriptor.psiElement.containingFile.virtualFile.toNioPath().parent.toFile()
        val newFile = file.resolve((descriptor.psiElement as XmlAttribute).value!!)
        try {
            if (!newFile.createNewFile()) {
                throw IOException("Failed to create file")
            }

            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(newFile, true)!!, true)
        } catch (e: Exception) {
            MessageBusUtil.showMessage(
                NotificationType.ERROR,
                "Failed to create file ${newFile.absoluteFile}",
                "Error message: ${e.message}"
            )
        }
    }
}
