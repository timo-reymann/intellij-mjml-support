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

class MjmlAttributeTypeBooleanCompletionContributor : CompletionContributor() {
    private val LOOKUP_OPTIONS = listOf(
        LookupElementBuilder.create("true"),
        LookupElementBuilder.create("false")
    )

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlAttributeValue::class.java),
            object : MjmlAttributeCompletionProvider(MjmlAttributeType.BOOLEAN) {
                override fun provide(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                    mjmlAttribute: MjmlAttributeInformation
                ) = result.addAllElements(LOOKUP_OPTIONS)
            })
    }
}
