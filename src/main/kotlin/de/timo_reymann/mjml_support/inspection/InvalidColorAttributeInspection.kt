package de.timo_reymann.mjml_support.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool
import com.intellij.psi.xml.XmlAttribute
import de.timo_reymann.mjml_support.bundle.MjmlBundle

import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.model.getMjmlTagFromAttribute
import de.timo_reymann.mjml_support.util.ColorUtil

class InvalidColorAttributeInspection : HtmlLocalInspectionTool() {
    override fun checkAttribute(attribute: XmlAttribute, holder: ProblemsHolder, isOnTheFly: Boolean) {
        val mjmlTag = getMjmlTagFromAttribute(attribute) ?: return
        val mjmlAttribute = mjmlTag.getAttributeByName(attribute.name) ?: return
        if (mjmlAttribute.type != MjmlAttributeType.COLOR) {
            return
        }

        val color = ColorUtil.parseColor(attribute.value)
        if (color == null) {
            holder.registerProblem(
                attribute,
                MjmlBundle.message("inspections.invalid_color"),
                ProblemHighlightType.ERROR,
            )
        }
    }
}
