package de.timo_reymann.mjml_support.reference.component

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.html.HtmlTag
import de.timo_reymann.mjml_support.api.MjmlTagInformationProvider

class MjmlComponentReference(element: PsiElement,private val text : String, private val textRange: TextRange) : PsiReferenceBase<PsiElement>(element, textRange) {
    override fun resolve(): PsiElement? {
        for (extension in MjmlTagInformationProvider.EXTENSION_POINT.extensions) {
            val references = extension.getPsiElements(element.project, text)
            if(references.isNotEmpty()) {
                    return references[0].second
            }
        }
        return null
    }

    override fun getVariants(): Array<Any> {
        for (extension in MjmlTagInformationProvider.EXTENSION_POINT.extensions) {
            val references = extension.getPsiElements(element.project, text)
            if(references.isNotEmpty()) {
                val ref = references[0]
                return arrayOf(LookupElementBuilder.create(ref.first.tagName))
            }
        }
        return arrayOf()
    }

    override fun bindToElement(element: PsiElement): PsiElement {
        return super.bindToElement(element)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if(element is com.intellij.psi.html.HtmlTag) {
            val name = element.name
            return name == (myElement as HtmlTag).name
        }
       return false
    }

    override fun isSoft(): Boolean {
        return false
    }
}

