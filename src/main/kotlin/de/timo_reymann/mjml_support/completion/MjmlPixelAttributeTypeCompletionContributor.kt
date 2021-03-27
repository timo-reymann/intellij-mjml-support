package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.model.MjmlAttributeType
import de.timo_reymann.mjml_support.model.getMjmlInfoFromAttributeValue

class MjmlPixelAttributeTypeCompletionContributor : CompletionContributor() {

    init {
        val pixelAttributeCompletionProvider = object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(
                parameters: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet
            ) {
                val target = parameters.position
                val (mjmlTag, mjmlAttribute) = getMjmlInfoFromAttributeValue(target)
                if (mjmlAttribute?.type != MjmlAttributeType.PIXEL) {
                    return
                }

                val text = getText(parameters)

                // Check if is a valid number
                text.toDoubleOrNull() ?: return

                result.addElement(LookupElementBuilder.create(text + UnitPrefix))
            }
        }

        extend(
            CompletionType.BASIC,
            psiElement().inside(XmlAttributeValue::class.java),
            pixelAttributeCompletionProvider
        )
    }

    companion object {
        const val UnitPrefix = "px"
    }

}
