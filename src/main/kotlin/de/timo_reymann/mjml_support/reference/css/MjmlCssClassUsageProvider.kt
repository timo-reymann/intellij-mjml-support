package de.timo_reymann.mjml_support.reference.css

import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlToken
import com.intellij.util.indexing.FileBasedIndex
import de.timo_reymann.mjml_support.index.MjmlClassUsageIndex
import de.timo_reymann.mjml_support.index.MjmlIncludeIndex
import de.timo_reymann.mjml_support.index.getFilesWithIncludesFor
import de.timo_reymann.mjml_support.util.isSameFile

/**
 * Mark usages of css-class for mj-style blocks
 */
class MjmlCssClassUsageProvider : CssClassOrIdReferenceBasedUsagesProvider() {
    override fun isUsage(selectorSuffix: CssSelectorSuffix, candidate: PsiElement, offsetInCandidate: Int): Boolean {
        // Only check for class selector + no self reference inside mj-style
        if (selectorSuffix !is CssClass || candidate is XmlToken) {
            return false
        }

        val project = candidate.project
        val selectorFile = selectorSuffix.containingFile.virtualFile
        val includesOfCssFile = getFilesWithIncludesFor(selectorSuffix.containingFile.virtualFile, project)

        // not used in any include -> cant be used
        if (includesOfCssFile.isEmpty()) {
            return false
        }

        /*
        Check if class is used somewhere where the file is included,
        this DOES NOT include recursive checks, so if you e.g. use one include for css and another for a partial
        the usage won't be declared.

        This is currently intended and if this is to strict the second solution would be to simply ignore the includes
        and assume if the class has been used anywhere and is included in any file it can be used an mark it as such.
         */

        return FileBasedIndex.getInstance()
            .getContainingFiles(
                MjmlClassUsageIndex.KEY,
                selectorSuffix.name!!,
                GlobalSearchScope.allScope(project)
            )
            .any { includesOfCssFile.contains(it) || isSameFile(selectorFile, it) }
    }
}
