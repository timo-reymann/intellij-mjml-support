package de.timo_reymann.mjml_support.xml

import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons.Nodes.Tag
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlTagNameProvider
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.isInMjmlFile
import de.timo_reymann.mjml_support.model.MjmlTagProvider

const val ROOT_TAG = "mjml"

class MjmlTagNameProvider : XmlTagNameProvider, XmlElementDescriptorProvider {
    override fun addTagNameVariants(elements: MutableList<LookupElement>, tag: XmlTag, prefix: String?) {
        if (!isInMjmlFile(tag)) {
            return
        }

        // Clear existing html elements
        elements.clear()

        var filter = fun(_: MjmlTagInformation): Boolean = true

        when {
            tag.parent is XmlTag -> {
                val parentTagName = (tag.parent as XmlTag).name
                val parentTag = MjmlTagProvider.getByTagName(tag.project, parentTagName) ?: return
                filter = fun(tagInfo: MjmlTagInformation): Boolean = tagInfo.isValidParent(parentTag)
            }
            tag.parent is PsiElement && tag.parent.elementType == XmlElementType.HTML_DOCUMENT -> {
                filter = fun(tagInfo: MjmlTagInformation): Boolean = tagInfo.tagName == ROOT_TAG
            }
        }

        MjmlTagProvider.getAll(tag.project)
            .filter { filter(it) }
            .mapTo(elements) {
                LookupElementBuilder.create(it.tagName)
                    .withTypeText("MJML component")
                    .withIcon(Tag)
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
