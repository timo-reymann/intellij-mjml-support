package de.timo_reymann.mjml_support.model

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag

private val EMPTY_PAIR = Pair<MjmlTagInformation?, MjmlAttributeInformation?>(null, null)

fun getMjmlInfoFromAttribute(attribute: XmlAttribute): Pair<MjmlTagInformation?, MjmlAttributeInformation?> {
    val tag = attribute.parentOfType<XmlTag>() ?: return EMPTY_PAIR
    val mjmlTag = MjmlTagProvider.getByXmlElement(tag) ?: return EMPTY_PAIR
    val mjmlAttribute = mjmlTag.getAttributeByName(attribute.name) ?: return EMPTY_PAIR

    return Pair(mjmlTag, mjmlAttribute)
}

fun getMjmlInfoFromAttributeValue(context: PsiElement): Pair<MjmlTagInformation?, MjmlAttributeInformation?> {
    val attributeValue = context.parentOfType<XmlAttributeValue>() ?: return EMPTY_PAIR
    val attribute = attributeValue.parentOfType<XmlAttribute>() ?: return EMPTY_PAIR
    return getMjmlInfoFromAttribute(attribute)
}
