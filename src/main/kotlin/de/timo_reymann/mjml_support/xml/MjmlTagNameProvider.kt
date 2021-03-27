package de.timo_reymann.mjml_support.xml

import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import de.timo_reymann.mjml_support.icons.MjmlIcons
import de.timo_reymann.mjml_support.isInMjmlFile
import de.timo_reymann.mjml_support.model.MjmlTagProvider

class MjmlTagNameProvider : XmlTagNameProvider, XmlElementDescriptorProvider {
    override fun addTagNameVariants(elements: MutableList<LookupElement>, tag: XmlTag, prefix: String?) {
        if (!isInMjmlFile(tag)) {
            return
        }

        // Clear existing html elements
        elements.clear()

        MjmlTagProvider.getAll().entries.mapTo(elements) {
            LookupElementBuilder.create(it.value.tagName)
                .withTypeText("mjml builtin component")
                .withIcon(MjmlIcons.COLORED)
                .withInsertHandler(XmlTagInsertHandler.INSTANCE)
        }
    }

    override fun getDescriptor(tag: XmlTag): XmlElementDescriptor? {
        if (!isInMjmlFile(tag)) {
            return null
        }

        return MjmlTagDescriptor(tag.name, tag)
    }
}
