package de.timo_reymann.mjml_support.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool
import com.intellij.psi.xml.XmlTag
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.model.MjmlTagProvider

class InvalidParentTagInspection : HtmlLocalInspectionTool() {
    override fun checkTag(tag: XmlTag, holder: ProblemsHolder, isOnTheFly: Boolean) {
        val mjmlTag = MjmlTagProvider.getByXmlElement(tag) ?: return
        if (tag.parent is XmlTag) {
            val parentTagName = (tag.parent as XmlTag).name
            val parentTag = MjmlTagProvider.getByTagName(tag.project, parentTagName) ?: return
            val allowedParentTags = mjmlTag.allowedParentTags

            if (mjmlTag.isValidParent(parentTag)) {
                return
            }

            val inspectionAllowedTagsText = when {
                allowedParentTags.isEmpty() -> "top level document"
                else -> allowedParentTags.joinToString(", ")
            }

            val errorMessage = when (parentTag.canHaveChildren) {
                true -> MjmlBundle.message(
                    "inspections.invalid_parent",
                    mjmlTag.tagName,
                    parentTagName,
                    inspectionAllowedTagsText
                )
                false ->  MjmlBundle.message(
                    "inspections.no_children_allowed",
                    parentTag.tagName
                )
            }

            holder.registerProblem(
                tag,
                errorMessage,
                ProblemHighlightType.WARNING
            )

        }
    }
}
