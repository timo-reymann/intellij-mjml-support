package de.timo_reymann.mjml_support.lang.model

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlTagInformation

private val EMPTY_PAIR = Pair<MjmlTagInformation?, MjmlAttributeInformation?>(null, null)

fun getMjmlTagFromAttribute(attribute: XmlAttribute): MjmlTagInformation? {
    val tag = attribute.parentOfType<XmlTag>() ?: return null
    return MjmlTagProvider.getByXmlElement(tag)
}

fun getMjmlInfoFromAttribute(attribute: XmlAttribute): Pair<MjmlTagInformation?, MjmlAttributeInformation?> {
    val mjmlTag = getMjmlTagFromAttribute(attribute) ?: return EMPTY_PAIR
    val mjmlAttribute = mjmlTag.getAttributeByName(attribute.name) ?: return EMPTY_PAIR
    return Pair(mjmlTag, mjmlAttribute)
}

fun getMjmlInfoFromAttributeValue(context: PsiElement): Pair<MjmlTagInformation?, MjmlAttributeInformation?> {
    val attributeValue = context.parentOfType<XmlAttributeValue>() ?: return EMPTY_PAIR
    val attribute = attributeValue.parentOfType<XmlAttribute>() ?: return EMPTY_PAIR
    return getMjmlInfoFromAttribute(attribute)
}
