package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.index.FileIndexUtil
import de.timo_reymann.mjml_support.inspection.IncludeType


class MjmlPathAttributeTypeCompletionContributor : CompletionContributor() {

    private val mjPathCompletion = object : MjmlAttributeCompletionProvider(MjmlAttributeType.PATH) {
        override fun provide(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
            mjmlTag: MjmlTagInformation?,
            mjmlAttribute: MjmlAttributeInformation
        ) {
            val target = parameters.position
            val xmlTag = target.parentOfType<XmlTag>() ?: return
            val includeFileType = IncludeType.fromTag(xmlTag).fileType
            val project = target.project
            val rootFile = target.containingFile.originalFile.virtualFile

            val fileNames =
                FileIndexUtil.getMatchesForAutoComplete(
                    project,
                    rootFile,
                    setOf(includeFileType),
                    parameters.invocationCount
                )

            for (path in fileNames) {
                result.addElement(
                    LookupElementBuilder
                        .create(path)
                        .withIcon(includeFileType.icon)
                )
            }
        }
    }

    init {
        extend(CompletionType.BASIC, psiElement().inside(XmlAttributeValue::class.java), mjPathCompletion)
    }
}
