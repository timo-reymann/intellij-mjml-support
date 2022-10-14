package de.timo_reymann.mjml_support.reference.css

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import de.timo_reymann.mjml_support.index.MjmlClassDefinition
import de.timo_reymann.mjml_support.index.MjmlClassDefinitionType

abstract class MjmlClassReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement>(element, textRange) {
    override fun isSoft(): Boolean = true
}

class CssDefinedClassReferenceImpl(element: PsiElement, textRange: TextRange, private val target: PsiElement) :
    MjmlClassReference(element, textRange) {
    override fun resolve(): PsiElement = target
}

class MjmlDefinedClassReferenceImpl(
    element: PsiElement,
    private val mjmlClassDefinition: MjmlClassDefinition,
    private val virtualFile: VirtualFile,
    textRange: TextRange
) : MjmlClassReference(element, textRange) {
    override fun resolve(): PsiElement? {
        val project = element.project
        val psi = PsiManager.getInstance(project)
            .findFile(virtualFile) ?: return null

        return when (mjmlClassDefinition.type) {
            MjmlClassDefinitionType.MJ_STYLE -> getFromMjStyle(project, psi)
            MjmlClassDefinitionType.MJML_CLASS -> getFromMjClass(psi)
        }
    }

    private fun getFromMjClass(psi: PsiFile): PsiElement? {
        return psi.findElementAt(mjmlClassDefinition.textOffset)?.parent
    }

    private fun getFromMjStyle(
        project: Project,
        psi: PsiFile
    ) = InjectedLanguageManager.getInstance(project)
        .findInjectedElementAt(psi, mjmlClassDefinition.textOffset)?.parent

}
