package de.timo_reymann.mjml_support.index.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.impl.stubs.index.CssClassIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.CommonProcessors
import com.intellij.util.indexing.FileBasedIndex
import de.timo_reymann.mjml_support.index.MjmlClassDefinition
import de.timo_reymann.mjml_support.index.MjmlClassDefinitionIndex
import de.timo_reymann.mjml_support.index.MjmlIncludeIndex

fun getCssDefinedClasses(project: Project, className: String): ArrayList<PsiElement> {
    val cssClasses = ArrayList<PsiElement>()
    StubIndex.getInstance()
        .processElements(
            CssClassIndex.KEY,
            className,
            project,
            GlobalSearchScope.allScope(project),
            CssSelectorSuffix::class.java,
            CommonProcessors.CollectProcessor(cssClasses)
        )
    return cssClasses
}

fun getMjmlDefinedClasses(
    project: Project,
    className: String
): MutableList<Pair<MjmlClassDefinition, VirtualFile>> {
    val occurrences = mutableListOf<Pair<MjmlClassDefinition, VirtualFile>>()
    FileBasedIndex.getInstance()
        .processValues(
            MjmlClassDefinitionIndex.KEY,
            className,
            null,
            { virtualFile, mjmlClassDefinition ->
                occurrences.add(
                    Pair<MjmlClassDefinition, VirtualFile>(
                        mjmlClassDefinition,
                        virtualFile
                    )
                )
                true
            },
            GlobalSearchScope.allScope(project)
        )
    return occurrences
}

fun isReachableFromReferencingElement(
    project: Project,
    usageFile: PsiFile,
    cssSelectorFile: VirtualFile
): Boolean {
    // Prevent jar files etc.
    if (!cssSelectorFile.isWritable) {
        return false
    }

    return FileBasedIndex.getInstance()
        .getContainingFiles(
            MjmlIncludeIndex.KEY,
            MjmlIncludeIndex.createIndexKey(cssSelectorFile),
            GlobalSearchScope.allScope(project)
        )
        .contains(usageFile.containingFile.virtualFile)
}
