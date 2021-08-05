package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.css.impl.stubs.index.CssClassIndex
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.icons.MjmlIcons
import de.timo_reymann.mjml_support.index.MjmlClassDefinitionIndex

/**
 * Experimental completion -> suggests not reachable files atm
 */
class MjmlClassCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlAttributeValue::class.java),
            object : MjmlAttributeCompletionProvider(MjmlAttributeType.CLASS) {
                override fun provide(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                    mjmlAttribute: MjmlAttributeInformation
                ) {
                    val classListResult: CompletionResultSet
                    var prefix = result.prefixMatcher.prefix
                    val lastSpace: Int = prefix.lastIndexOf(' ')
                    if (lastSpace >= 0 && lastSpace < prefix.length - 1) {
                        prefix = prefix.substring(lastSpace + 1)
                        classListResult = result.withPrefixMatcher(prefix)
                    } else {
                        classListResult = result
                    }

                    val project = parameters.position.project

                    StubIndex.getInstance()
                        .getAllKeys(CssClassIndex.KEY, project)
                        .forEach {
                            classListResult.addElement(LookupElementBuilder.create(it).withIcon(AllIcons.FileTypes.Css))
                        }

                    FileBasedIndex.getInstance()
                        .getAllKeys(MjmlClassDefinitionIndex.KEY, project)
                        .forEach {
                            classListResult.addElement(LookupElementBuilder.create(it).withIcon(MjmlIcons.COLORED))
                        }
                }
            })

    }
}
