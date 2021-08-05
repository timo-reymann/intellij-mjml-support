package de.timo_reymann.mjml_support.util

import com.intellij.psi.xml.XmlAttribute

object MjmlPsiUtil {
    fun isAnyClassAttribute(attribute : XmlAttribute): Boolean {
        return isMjmlClass(attribute) || isHtmlClass(attribute)
    }

    fun isMjmlClass(attribute : XmlAttribute): Boolean {
        return attribute.name == "css-class"
    }

    fun isHtmlClass(attribute : XmlAttribute): Boolean {
        return attribute.name == "class"
    }
}
