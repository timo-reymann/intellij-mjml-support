package de.timo_reymann.mjml_support.index

import com.intellij.lang.css.CSSLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.css.CssElementFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import java.io.DataInput
import java.io.DataOutput

class MjmlClassDefinitionIndex : AbstractMjmlFileBasedIndex<MjmlClassDefinition>(3) {
    companion object {
        val KEY = ID.create<String, MjmlClassDefinition>(MjmlClassDefinitionIndex::class.java.canonicalName)
    }

    override fun getName(): ID<String, MjmlClassDefinition> = KEY

    override fun getIndexer(): DataIndexer<String, MjmlClassDefinition, FileContent> {
        return DataIndexer<String, MjmlClassDefinition, FileContent> { fileContent ->
            val result = mutableMapOf<String, MjmlClassDefinition>()
            val project = fileContent.project

            val relevantTags = PsiTreeUtil.findChildrenOfType(fileContent.psiFile, XmlTag::class.java)
                .filter { it.name == "mj-style" || it.name == "mj-class" }

            relevantTags
                .filter { it.name == "mj-style" }
                .forEach { xmlTag ->
                    PsiTreeUtil.findChildrenOfType(xmlTag, XmlText::class.java)
                        .firstOrNull()
                        ?.also {
                            val inlineStyleSheet = it.text
                            val styleSheet =
                                CssElementFactory.getInstance(project).createStylesheet(inlineStyleSheet, CSSLanguage.INSTANCE)
                            val rangeBegin = it.textOffset

                            for (ruleSet in styleSheet.rulesetList.rulesets) {
                                for (selector in ruleSet.selectors) {
                                    for (selectorPart in selector.simpleSelectors) {
                                        val selectorText = selectorPart.text

                                        if (!selectorText.startsWith('.')) {
                                            continue
                                        }

                                        val selectorClassName = selectorPart.text.substring(1)
                                        result[selectorClassName] = MjmlClassDefinition(
                                            rangeBegin + ruleSet.textOffset,
                                            TextRange(ruleSet.textRange.startOffset + rangeBegin, ruleSet.textRange.endOffset + rangeBegin),
                                            MjmlClassDefinitionType.MJ_STYLE
                                        )
                                    }
                                }
                            }

                        }
                    // TODO Index mj-class
                }

            result
        }
    }

    override fun getValueExternalizer(): DataExternalizer<MjmlClassDefinition> {
        return object : DataExternalizer<MjmlClassDefinition> {
            override fun save(out: DataOutput, value: MjmlClassDefinition?) {
                value ?: return
                out.writeInt(value.textOffset)
                out.writeInt(value.textRange.startOffset)
                out.writeInt(value.textRange.endOffset)
                out.writeInt(value.type.ordinal)
            }

            override fun read(dataInput: DataInput): MjmlClassDefinition {
                val textOffset = dataInput.readInt()
                val startOffset = dataInput.readInt()
                val endOffset = dataInput.readInt()
                val ordinal = dataInput.readInt()

                return MjmlClassDefinition(
                    textOffset,
                    TextRange(startOffset, endOffset),
                    MjmlClassDefinitionType.values()[ordinal]
                )
            }
        }
    }
}
