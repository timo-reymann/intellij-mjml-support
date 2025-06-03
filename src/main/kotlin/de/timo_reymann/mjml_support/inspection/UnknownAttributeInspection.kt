package de.timo_reymann.mjml_support.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlAttribute
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.lang.model.getMjmlTagFromAttribute

class UnknownAttributeInspection : HtmlLocalInspectionTool() {
    override fun checkAttribute(attribute: XmlAttribute, holder: ProblemsHolder, isOnTheFly: Boolean) {
        val mjmlTag = getMjmlTagFromAttribute(attribute) ?: return
        if (mjmlTag.getAttributeByName(attribute.name) != null) {
            return
        }

        holder.registerProblem(
            attribute,
            MjmlBundle.message("inspections.unknown_attribute", attribute.name, mjmlTag.tagName),
            ProblemHighlightType.WARNING,
            RemoveUnknownAttributeQuickFix()
        )
        super.checkAttribute(attribute, holder, isOnTheFly)
    }
}

class RemoveUnknownAttributeQuickFix : LocalQuickFix {
    override fun getFamilyName(): String = "Remove unknown attribute"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (descriptor.psiElement !is XmlAttribute) {
            return
        }
        descriptor.psiElement.delete()
    }

}
