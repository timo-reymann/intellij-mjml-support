package de.timo_reymann.mjml_support.index

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectScope
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.webcore.template.TemplateLanguageFileUtil
import java.io.File
import java.util.*

object FileIndexUtil {
    fun getMatchesForAutoComplete(project: Project, rootFile: VirtualFile, fileTypes: Set<FileType>, invocationCount: Int): MutableSet<String> {
        val resultNames: MutableSet<String> = TreeSet()
        val results: MutableSet<String> = mutableSetOf()

        processAllNames(project) { fileName: String ->
            for(fileType in fileTypes) {
                if (filenameMatchesPrefixOrType(fileType, fileName, invocationCount)) {
                    resultNames.add(fileName)
                }
            }
            true
        }

        val scope = ProjectScope.getProjectScope(project)

        for (resultName in resultNames) {

            val files = FilenameIndex.getFilesByName(project, resultName, scope)

            if (files.isEmpty()) {
                continue
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
            } catch (ex: Exception) {
            }
        }
    }

    private fun filenameMatchesPrefixOrType(fileType: FileType, fileName: String, invocationCount: Int): Boolean {
        if (invocationCount > 2) {
            return true
        }

        val extension = FileUtilRt.getExtension(fileName)
        if (extension.isEmpty()) {
            return false
        }

        for (matcher in FileTypeManager.getInstance().getAssociations(fileType)) {
            if (matcher.acceptsCharSequence(fileName)) return true
        }

        return false
    }
}
