package de.timo_reymann.mjml_support.index

import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.KeyDescriptor
import de.timo_reymann.mjml_support.util.MjmlPsiUtil
import de.timo_reymann.mjml_support.util.TextRangeUtil
import java.io.DataInput
import java.io.DataOutput

class MjmlClassUsageIndex : AbstractMjmlFileBasedIndex<MjmlClassUsage>(3) {
    companion object {
        val KEY = ID.create<String, MjmlClassUsage>(MjmlClassUsageIndex::class.java.canonicalName)
    }

    private val indexer = DataIndexer<String, MjmlClassUsage, FileContent> { fileContent ->
        val result = mutableMapOf<String, MjmlClassUsage>()

        PsiTreeUtil.findChildrenOfType(fileContent.psiFile, XmlAttribute::class.java)
            .filter { MjmlPsiUtil.isAnyClassAttribute(it) }
            .forEach { attribute ->

                val classes = attribute.value?.split(' ') ?: return@forEach
                classes.forEach { clazz ->
                    result[clazz] = MjmlClassUsage(
                        attribute.textOffset,
                        TextRangeUtil.fromString(attribute.value!!, clazz)
                    )
                }
            }

        return@DataIndexer result
    }

    private val valueExternalizer = object : KeyDescriptor<MjmlClassUsage> {
        override fun getHashCode(value: MjmlClassUsage?): Int = value.hashCode()

        override fun isEqual(val1: MjmlClassUsage?, val2: MjmlClassUsage?): Boolean =
            val1?.equals(val2) ?: false

        override fun save(out: DataOutput, value: MjmlClassUsage?) {
            value ?: return

            out.writeInt(value.textOffset)
            out.writeInt(value.classTextRange.startOffset)
            out.writeInt(value.classTextRange.endOffset)
        }

        override fun read(dataInput: DataInput): MjmlClassUsage {
            val textOffset = dataInput.readInt()
            val startOffset = dataInput.readInt()
            val endOffset = dataInput.readInt()
            return MjmlClassUsage(textOffset, TextRange(startOffset, endOffset))
        }
    }

    override fun getName(): ID<String, MjmlClassUsage> = KEY

    override fun getIndexer(): DataIndexer<String, MjmlClassUsage, FileContent> = indexer

    override fun getValueExternalizer(): DataExternalizer<MjmlClassUsage> = valueExternalizer

}
