package de.timo_reymann.mjml_support.api

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlTag

abstract class MjmlTagInformationProvider {
    companion object {
        val EXTENSION_POINT = ExtensionPointName<MjmlTagInformationProvider>("de.timo_reymann.intellij-mjml-support.tagInformationProvider")
    }

    /**
     * Resolve a tag by its name, if it cant be resolved the implementation should return null
     *
     * @param tagName Custom tag name
     */
    abstract fun getByTagName(project: Project, tagName: String): MjmlTagInformation?

    /**
     * Resolve a tag by its xml tag
     *
     * @param xmlTag Xml tag to resolve
     */
    fun getByXmlTag(xmlTag: XmlTag): MjmlTagInformation? {
        return getByTagName(xmlTag.project, xmlTag.name)
    }

    /**
     * Return all available tags for provider
     */
    abstract fun getAll(project: Project) : List<MjmlTagInformation>
}
