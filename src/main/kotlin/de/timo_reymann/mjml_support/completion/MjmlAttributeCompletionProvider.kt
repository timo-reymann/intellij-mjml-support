package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.lang.model.getMjmlInfoFromAttributeValue

abstract class MjmlAttributeCompletionProvider(private val attributeType: MjmlAttributeType) :
    CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val target = parameters.position
        val (mjmlTag, mjmlAttribute) = getMjmlInfoFromAttributeValue(target)
        if (mjmlAttribute?.type != attributeType) {
            return
        }
        provide(parameters, context, result, mjmlTag, mjmlAttribute)
    }

    abstract fun provide(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
        mjmlTag : MjmlTagInformation?,
        mjmlAttribute: MjmlAttributeInformation
    )
}
