package de.timo_reymann.mjml_support.util

import com.intellij.injected.editor.VirtualFileWindow
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
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

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

fun isCssBlockInMjmlFile(candidate: PsiElement): Boolean {
    val file = candidate.containingFile.viewProvider.virtualFile

    if (file !is VirtualFileWindow) {
        return false
    }

    return file.delegate.fileType == MjmlHtmlFileType.INSTANCE
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

    // if is no file or same file
    if(usageFile.virtualFile == null || isSameFile(cssSelectorFile, usageFile.virtualFile)) {
        return true
    }

    return FileBasedIndex.getInstance()
        .getContainingFiles(
            MjmlIncludeIndex.KEY,
            MjmlIncludeIndex.createIndexKey(cssSelectorFile),
            GlobalSearchScope.allScope(project)
        )
        .contains(usageFile.containingFile.virtualFile)
}

fun isSameFile(selectorFile : VirtualFile, targetFile : VirtualFile): Boolean {
    return selectorFile == targetFile ||
            (selectorFile is VirtualFileWindow && (selectorFile as VirtualFileWindow).delegate == targetFile)
}
