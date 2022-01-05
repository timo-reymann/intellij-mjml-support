package de.timo_reymann.mjml_support.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.htmlInspections.HtmlLocalInspectionTool
import com.intellij.openapi.paths.GlobalPathReferenceProvider
import com.intellij.psi.xml.XmlAttribute
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.model.getMjmlTagFromAttribute
import de.timo_reymann.mjml_support.settings.MjmlSettings
import java.net.URL

class InvalidUrlAttributeInspection : HtmlLocalInspectionTool() {
    override fun checkAttribute(attribute: XmlAttribute, holder: ProblemsHolder, isOnTheFly: Boolean) {
        val mjmlTag = getMjmlTagFromAttribute(attribute) ?: return
        val mjmlAttribute = mjmlTag.getAttributeByName(attribute.name) ?: return
        if (mjmlAttribute.type != MjmlAttributeType.URL) {
            return
        }

        // Disable inspection in case local images are enabled
        if (MjmlSettings.getInstance(attribute.project).resolveLocalImages) {
            return
        }

        // Check text has been set
        attribute.value ?: return

        // Validate url
        var isValidUrl = true
        try {
            URL(attribute.value)
        } catch (e: Exception) {
            isValidUrl = false
        }

        // Validate prefix
        val isValidPrefix = GlobalPathReferenceProvider.isWebReferenceUrl(attribute.value) ||
                GlobalPathReferenceProvider.startsWithAllowedPrefix(attribute.value)

        // Everything fine
        if (!isValidPrefix && !isValidUrl) {
            holder.registerProblem(
                attribute,
                MjmlBundle.message("inspections.invalid_url"),
                ProblemHighlightType.WARNING,
            )
        }
    }
}
