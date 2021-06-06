package de.timo_reymann.mjml_support.reference

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.model.MjmlTagProvider

class MjmlComponentDefinedClassUsageProvider : CssClassOrIdReferenceBasedUsagesProvider() {
    override fun acceptElement(candidate: PsiElement): Boolean {
        return candidate is CssClass
                && (candidate.containingFile.viewProvider.virtualFile as VirtualFileWindow).delegate.fileType == MjmlHtmlFileType.INSTANCE
    }

    /**
     * Find any usage of an defined css class by an mjml component, even if the component is not defined in the current file.
     * This makes sure the custom class is also working for includes and custom components acting as wrappers
     *
     * @see #acceptElement() for element target evaluation
     */
    override fun isUsage(selectorSuffix: CssSelectorSuffix, candidate: PsiElement, offsetInCandidate: Int): Boolean {
        if (selectorSuffix !is CssClass || !acceptElement(candidate)) {
            return false
        }

        val className = selectorSuffix.name ?: return false

        return MjmlTagProvider.getAll(candidate.project)
            .any { it.definesClass(className) }
    }
}
