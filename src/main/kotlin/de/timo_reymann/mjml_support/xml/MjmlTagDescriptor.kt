package de.timo_reymann.mjml_support.xml

import com.intellij.codeInsight.daemon.impl.analysis.XmlHighlightVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import de.timo_reymann.mjml_support.model.MjmlTagProvider

class MjmlTagDescriptor(private val tagName: String, private val xmlTag: XmlTag) : XmlElementDescriptor {
    override fun getDeclaration() = xmlTag

    override fun getName(context: PsiElement?) = tagName

    override fun getName(): String = tagName

    override fun init(element: PsiElement?) {}

    override fun getQualifiedName() = tagName

    override fun getDefaultName() = tagName

    /**
    Prevent further validation
     */
    private fun disableValidation(xmlTag : XmlTag?) {
        xmlTag ?: return
        XmlHighlightVisitor.setSkipValidation(xmlTag)
    }

    override fun getElementsDescriptors(context: XmlTag?): Array<XmlElementDescriptor> {
        disableValidation(context)
        return XmlDescriptorUtil.getElementsDescriptors(context)
    }

    override fun getElementDescriptor(childTag: XmlTag?, contextTag: XmlTag?): XmlElementDescriptor? {
        disableValidation(contextTag)
        return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag)
    }

    override fun getAttributesDescriptors(context: XmlTag?): Array<XmlAttributeDescriptor> {
        if (context == null) {
            return arrayOf()
        }

        val tag = MjmlTagProvider.getByTagName(context.project, context.name) ?: return arrayOf()

        return tag.attributes.map {
            MjmlTagAttributeDescriptor(it.name, context)
        }.toTypedArray()
    }

    override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor {
        return AnyXmlAttributeDescriptor(attributeName)
    }

    override fun getAttributeDescriptor(attribute: XmlAttribute): XmlAttributeDescriptor {
        return getAttributeDescriptor(attribute.name, attribute.parent)
    }

    override fun getNSDescriptor(): Nothing? = null

    override fun getTopGroup(): Nothing? = null

    override fun getContentType(): Int = XmlElementDescriptor.CONTENT_TYPE_ANY

    override fun getDefaultValue(): String? = null

}
