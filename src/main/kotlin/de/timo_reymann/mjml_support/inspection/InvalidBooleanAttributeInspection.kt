package de.timo_reymann.mjml_support.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool
import com.intellij.psi.xml.XmlAttribute
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.lang.model.getMjmlTagFromAttribute

class InvalidBooleanAttributeInspection : HtmlLocalInspectionTool() {
    override fun checkAttribute(attribute: XmlAttribute, holder: ProblemsHolder, isOnTheFly: Boolean) {
        val mjmlTag = getMjmlTagFromAttribute(attribute) ?: return
        val mjmlAttribute = mjmlTag.getAttributeByName(attribute.name) ?: return
        if (mjmlAttribute.type != MjmlAttributeType.BOOLEAN) {
            return
        }

        val value = attribute.value
        if (value == "true" || value == "false") {
            return
        }

        holder.registerProblem(
            attribute,
            MjmlBundle.message("inspections.invalid_boolean"),
            ProblemHighlightType.WARNING,
        )
    }
}
