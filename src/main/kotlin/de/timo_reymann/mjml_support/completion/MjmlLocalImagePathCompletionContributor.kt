package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.icons.FileTypeIcons
import de.timo_reymann.mjml_support.index.FileIndexUtil
import de.timo_reymann.mjml_support.settings.MjmlSettings
import org.intellij.images.fileTypes.impl.ImageFileType

class MjmlLocalImagePathCompletionContributor : CompletionContributor() {
    companion object {
        const val MJ_IMAGE_TAG = "mj-image"
    }

    private val localImagePathCompletion = object : MjmlAttributeCompletionProvider(MjmlAttributeType.URL) {
        override fun provide(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
            mjmlTag: MjmlTagInformation?,
            mjmlAttribute: MjmlAttributeInformation
        ) {
            val target = parameters.position
            val project = target.project
            val rootFile = target.containingFile.originalFile.virtualFile

            if (mjmlTag?.tagName != MJ_IMAGE_TAG) {
                return
            }

            if (!MjmlSettings.getInstance(project).resolveLocalImages) {
                return
            }

            val imageFiles = FileIndexUtil.getMatchesForAutoComplete(project, rootFile, setOf(
                ImageFileType.INSTANCE
            ), parameters.invocationCount)

            for (path in imageFiles) {
                result.addElement(
                    LookupElementBuilder
                        .create(path)
                        .withIcon(FileTypeIcons.Image)
                )
            }
        }
    }

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlAttributeValue::class.java),
            localImagePathCompletion
        )
    }
}
