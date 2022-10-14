package de.timo_reymann.mjml_support.index

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.openapi.fileTypes.FileNameMatcher
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.webcore.template.TemplateLanguageFileUtil
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage
import java.io.File
import java.util.*

object FileIndexUtil {
    fun getMatchesForAutoComplete(
        project: Project,
        rootFile: VirtualFile,
        fileTypes: Set<FileType>,
        invocationCount: Int
    ): MutableSet<String> {
        val results: MutableSet<String> = mutableSetOf()
        val scope = ProjectScope.getProjectScope(project)
        val files = mutableListOf<PsiFile>()

        val fileTypeMatcher = fileTypes.flatMap {
            FileTypeManager.getInstance().getAssociations(it)
        }

        processAllNames(project) { fileName: String ->
            files.addAll(
                getFilesMatchingPrefixOrType(
                    project,
                    scope,
                    fileTypeMatcher,
                    fileName,
                    invocationCount
                )
            )
            true
        }

        for (psiFile in files) {
            // ProgressManager.checkCanceled()
            val virtualFile: VirtualFile = psiFile.virtualFile ?: continue
            if (virtualFile == rootFile) {
                continue
            }

            val filePath: String = File(virtualFile.path).relativeTo(File(rootFile.parent.path)).toString()
            results.add(filePath)
        }

        return results
    }

    private fun processAllNames(project: Project, processor: Processor<in String>) {
        for (contributor in ChooseByNameContributor.FILE_EP_NAME.extensionList) {
            try {
                if (contributor is ChooseByNameContributorEx) {
                    contributor.processNames(processor, FindSymbolParameters.searchScopeFor(project, false), null)
                } else {
                    ContainerUtil.process(contributor.getNames(project, false), processor)
                }
            } catch (_: Exception) {
                // ignore resolve error and discard silently
            }
        }
    }

    private fun getFilesMatchingPrefixOrType(
        project: Project,
        scope: GlobalSearchScope,
        fileTypeMatcher: List<FileNameMatcher>,
        fileName: String,
        invocationCount: Int
    ): Array<PsiFile> {
        return when {
            invocationCount > 2 || isFileTypeMatch(fileName, fileTypeMatcher) -> {
                FilenameIndex.getFilesByName(project, fileName, scope)
            }
            else -> {
                FilenameIndex.getFilesByName(project, fileName, scope)
                    .filter {
                        TemplateLanguageFileUtil.getTemplateDataLanguage(
                            project,
                            it.virtualFile
                        ) == MjmlHtmlLanguage.INSTANCE
                    }.toTypedArray()
            }
        }
    }

    private fun isFileTypeMatch(fileName: String, fileTypeMatcher: List<FileNameMatcher>): Boolean {
        // Ignore files without extensions
        val extension = FileUtilRt.getExtension(fileName)
        if (extension.isEmpty()) {
            return false
        }

        return fileTypeMatcher.any { it.acceptsCharSequence(fileName) }
    }
}
