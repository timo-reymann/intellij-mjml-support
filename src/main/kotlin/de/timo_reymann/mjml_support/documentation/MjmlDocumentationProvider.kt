package de.timo_reymann.mjml_support.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.model.MjmlTagProvider

class MjmlDocumentationProvider : DocumentationProvider {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {

        if (element is XmlTag) {
            return generateTagDocumentation(element.project, element.name)
        } else if (element is XmlAttribute) {
            return generateAttributeDocumentation(element.project, element.parent.name, element.name)
        }
        return null
    }

    override fun getUrlFor(element: PsiElement, originalElement: PsiElement): MutableList<String> {
        if (element !is XmlTag && element !is XmlAttribute) {
            return mutableListOf()
        }

        val tagName = (when (element) {
            is XmlTag -> element.name
            is XmlAttribute -> element.parent.name
            else -> null
        }) ?: return mutableListOf()

        val mjmlTag = MjmlTagProvider.getByTagName(element.project, tagName) ?: return mutableListOf()
        return mutableListOf(MjmlBundle.message("documentation.url_pattern", mjmlTag.tagName))
    }

    private fun generateAttributeDocumentation(project: Project, tagName: String, attributeName: String): String? {
        val mjmlTag = MjmlTagProvider.getByTagName(project, tagName) ?: return null
        val attributesMatched = mjmlTag.attributes.filter { it.name == attributeName }
        if (attributesMatched.size != 1) {
            return null
        }
        val attribute = attributesMatched[0]
        val buf = StringBuilder()

        // Attribute name
        buf.append(DocumentationMarkup.DEFINITION_START)
            .append(attributeName)
            .append(DocumentationMarkup.DEFINITION_END)

        buf
            .append(DocumentationMarkup.CONTENT_START)
            .append(attribute.description)
            .append(DocumentationMarkup.CONTENT_END)

        val sections = mapOf(
            "Type" to attribute.type.description,
            "Default" to (attribute.defaultValue ?: "N/A")
        )

        buf.append(DocumentationMarkup.SECTIONS_START)
        for (entry in sections) {
            buf.append(DocumentationMarkup.SECTION_HEADER_START)
                .append(entry.key)
                .append(":")
                .append(DocumentationMarkup.SECTION_SEPARATOR)
                .append(entry.value)
                .append(DocumentationMarkup.SECTION_END)
        }
        buf.append(DocumentationMarkup.SECTIONS_END)

        return buf.toString()
    }

    private fun generateTagDocumentation(project: Project, tagName: String): String? {
        val mjmlTag = MjmlTagProvider.getByTagName(project, tagName) ?: return null

        val buf = StringBuilder()

        // Tag name
        buf.append(DocumentationMarkup.DEFINITION_START)
            .append(tagName)
            .append(DocumentationMarkup.DEFINITION_END)

        // Description
        buf
            .append(DocumentationMarkup.CONTENT_START)
            .append(StringUtil.capitalize(mjmlTag.description.replace("\n", "<br />")))
            .append(DocumentationMarkup.CONTENT_END)

        if (mjmlTag.notes.isNotEmpty()) {
            buf.append(DocumentationMarkup.CONTENT_START)
                .append("<br />")
                .append("<b>Notes</b>")
                .append("<br />")
                .append("<ul>")

            for (note in mjmlTag.notes) {
                buf
                    .append("<li>")
                    .append(note)
                    .append("</li>")
            }

            buf.append("</ul>")
                .append(DocumentationMarkup.CONTENT_END)
        }
        buf
            .append(DocumentationMarkup.CONTENT_START)
            .append(DocumentationMarkup.CONTENT_END)

        return buf.toString()
    }
}
