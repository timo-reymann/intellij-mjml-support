package de.timo_reymann.mjml_support.reference.css

import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.indexing.FileBasedIndex
import de.timo_reymann.mjml_support.index.MjClassIndex

/**
 * Mark usages of css-class for mj-style blocks
 */
class MjmlCssClassUsageProvider : CssClassOrIdReferenceBasedUsagesProvider() {
    override fun isUsage(selectorSuffix: CssSelectorSuffix, candidate: PsiElement, offsetInCandidate: Int): Boolean {
        if (selectorSuffix !is CssClass) {
            return false
        }

        // TODO Add second index to check if the selector was included or is directly inside the mjml-file inside a mj-style block

        return FileBasedIndex.getInstance()
            .getContainingFiles(MjClassIndex.KEY, selectorSuffix.name!!, GlobalSearchScope.allScope(candidate.project))
            .isNotEmpty()
    }
}
