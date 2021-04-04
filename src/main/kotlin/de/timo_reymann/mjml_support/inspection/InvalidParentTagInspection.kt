package de.timo_reymann.mjml_support.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool
import com.intellij.psi.xml.XmlTag
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.model.MjmlTagProvider
import de.timo_reymann.mjml_support.model.PARENT_ANY

class InvalidParentTagInspection : HtmlLocalInspectionTool() {
    override fun checkTag(tag: XmlTag, holder: ProblemsHolder, isOnTheFly: Boolean) {
        val mjmlTag = MjmlTagProvider.getByXmlElement(tag) ?: return
        if (tag.parent is XmlTag) {
            val parentTagName = (tag.parent as XmlTag).name
            val allowedParentTags = mjmlTag.allowedParentTags

            if (allowedParentTags == PARENT_ANY || allowedParentTags.contains(parentTagName)) {
                return
            }

            val inspectionAllowedTagsText = when {
                allowedParentTags.isEmpty() -> "top level document"
                else -> allowedParentTags.joinToString(", ")
            }

            holder.registerProblem(
                tag,
                MjmlBundle.message(
                    "inspections.invalid_parent",
                    mjmlTag.tagName,
                    parentTagName,
                    inspectionAllowedTagsText
                ),
                ProblemHighlightType.WARNING
            )

        }
    }
}
