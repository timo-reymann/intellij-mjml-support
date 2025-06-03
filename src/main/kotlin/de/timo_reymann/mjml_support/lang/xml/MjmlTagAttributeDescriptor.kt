package de.timo_reymann.mjml_support.lang.xml

import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.impl.BasicXmlAttributeDescriptor

class MjmlTagAttributeDescriptor(private val attributeName: String, private val tag: XmlTag) :
    BasicXmlAttributeDescriptor() {
    override fun isFixed() = false

    override fun getDefaultValue(): String? = null

    override fun isEnumerated() = false

    override fun getEnumeratedValues() = null

    override fun getDeclaration(): PsiElement = tag

    override fun getName(): String = attributeName

    override fun init(element: PsiElement?) = Unit

    override fun isRequired() = false

    override fun hasIdType() = false

    override fun hasIdRefType() = false

}
