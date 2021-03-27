package de.timo_reymann.mjml_support

import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage

fun getXmlName(xmlElement : PsiElement): String? {
    if (xmlElement is XmlTag) {
        return xmlElement.name
    }

    if (xmlElement is XmlAttribute) {
        return xmlElement.name
    }

    return null
}

fun isInMjmlFile(context : PsiElement) = context.containingFile.language is MjmlHtmlLanguage
