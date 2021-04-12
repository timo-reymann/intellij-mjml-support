package de.timo_reymann.mjml_support.model

import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlTag
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.api.MjmlTagInformationProvider
import org.jetbrains.annotations.NotNull

/**
 * Utility to get mjml tag information from all sources
 */
object MjmlTagProvider {
    private fun getProviders(): @NotNull Array<out MjmlTagInformationProvider> {
        return MjmlTagInformationProvider.EXTENSION_POINT.extensions
    }

    fun getByTagName(project: Project, tagName: String): MjmlTagInformation? {
        getProviders()
            .sortedBy { it.getPriority() }
            .reversed()
            .forEach {
                val tagInfo = it.getByTagName(project, tagName)
                if (tagInfo != null) {
                    return tagInfo
                }
            }

        return null
    }

    fun getByXmlElement(xmlTag: XmlTag): MjmlTagInformation? {
        getProviders().forEach {
            val tagInfo = it.getByXmlTag(xmlTag)
            if (tagInfo != null) {
                return tagInfo
            }
        }

        return null
    }

    fun getAll(project: Project): List<MjmlTagInformation> = getProviders()
        .map { it.getAll(project) }
        .flatten()
}

