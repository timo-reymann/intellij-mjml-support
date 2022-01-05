package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import com.intellij.xml.util.ColorSampleLookupValue
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation

class MjmlColorAttributeTypeCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlAttributeValue::class.java),
            object : MjmlAttributeCompletionProvider(MjmlAttributeType.COLOR) {
                override fun provide(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                    mjmlTag: MjmlTagInformation?,
                    mjmlAttribute: MjmlAttributeInformation
                ) {
                    result.addAllElements(ColorSampleLookupValue.getColors()
                        .map { it.toLookupElement() })
                }
            })
    }
}
