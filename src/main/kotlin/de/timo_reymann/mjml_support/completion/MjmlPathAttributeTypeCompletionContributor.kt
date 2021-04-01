package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectScope
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.FindSymbolParameters
import de.timo_reymann.mjml_support.icons.MjmlIcons
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.model.MjmlAttributeType
import de.timo_reymann.mjml_support.model.getMjmlInfoFromAttributeValue
import java.lang.Exception
import java.util.*


class MjmlPathAttributeTypeCompletionContributor : CompletionContributor() {

    private val mjPathCompletion = object : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            processingContext: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val target = parameters.position
            val (_, mjmlAttribute) = getMjmlInfoFromAttributeValue(target)
            if(mjmlAttribute?.type != MjmlAttributeType.PATH) {
                return
            }

            val project = target.project
            val rootFile = target.containingFile.originalFile.virtualFile
            val resultNames: MutableSet<String> = TreeSet()

            processAllNames(project, Processor { fileName: String ->
                if (filenameMatchesPrefixOrType(
                        fileName, parameters.invocationCount
                    )
                ) {
                    resultNames.add(fileName)
                }
                true
            })

            val scope = ProjectScope.getProjectScope(project)

            for (resultName in resultNames) {
                // ProgressManager.checkCanceled();

                val files = FilenameIndex.getFilesByName(project, resultName, scope);

                if (files.isEmpty()) {
                    continue;
                }

                for (psiFile in files) {
                   // ProgressManager.checkCanceled()
                    val virtualFile: VirtualFile = psiFile.virtualFile ?: continue
                    if (virtualFile == rootFile) {
                        continue
                    }

                    val filePath : String =virtualFile.toNioPath().toFile().relativeTo(rootFile.parent.toNioPath().toFile()).toString()
                    resultSet.addElement(LookupElementBuilder.create(filePath).withIcon(MjmlIcons.COLORED))
                }
            }
        }
    }

    private fun filenameMatchesPrefixOrType(fileName: String, invocationCount: Int): Boolean {
        if (invocationCount > 2) {
            return true
        }

        val extension = FileUtilRt.getExtension(fileName)
        if (extension.isEmpty()) {
            return false
        }

        for (matcher in FileTypeManager.getInstance().getAssociations(MjmlHtmlFileType.INSTANCE)) {
            if (matcher.acceptsCharSequence(fileName)) return true
        }

        return false
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

    init {
        extend(CompletionType.BASIC, psiElement().inside(XmlAttributeValue::class.java), mjPathCompletion)
    }
}
