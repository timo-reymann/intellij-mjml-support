package de.timo_reymann.mjml_support.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue

class MjmlCssClassUsageProvider : CssClassOrIdReferenceBasedUsagesProvider() {
    override fun acceptElement(candidate: PsiElement): Boolean {
        if(candidate !is XmlAttributeValue) {
            return false
        }

        val attribute = candidate.parentOfType<XmlAttribute>() ?: return false
        return attribute.name == "css-class"
    }

    override fun isUsage(selectorSuffix: CssSelectorSuffix, candidate: PsiElement, offsetInCandidate: Int): Boolean {
        if(selectorSuffix !is CssClass || candidate !is XmlAttributeValue) {
            return false
        }
        return candidate.value.split(" ").contains(selectorSuffix.name)
    }
}
