package de.timo_reymann.mjml_support.reference.component

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.PsiReferenceRegistrar.HIGHER_PRIORITY
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.reference.MJML_FILE_PATTERN

class MjmlComponentReferenceProvider : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(XmlTag::class.java).inFile(MJML_FILE_PATTERN),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {

                    if (element !is XmlTag) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    return arrayOf(
                        MjmlComponentReference(element, element.localName, TextRange(0, element.localName.length + 1))
                    )
                }
            },HIGHER_PRIORITY)
    }
}
