package de.timo_reymann.mjml_support.reference.css

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class MjmlClassReference(element: PsiElement, private val target : PsiElement, range: TextRange?) : PsiReferenceBase<PsiElement>(element, range) {
    override fun isSoft(): Boolean = true

    override fun resolve(): PsiElement = target
}