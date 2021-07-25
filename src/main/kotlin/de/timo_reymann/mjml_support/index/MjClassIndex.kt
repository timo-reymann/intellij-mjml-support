package de.timo_reymann.mjml_support.index

import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.util.TextRangeUtil
import java.io.DataInput
import java.io.DataOutput

class MjClassIndex : FileBasedIndexExtension<String, MjClassIndexEntry>() {
    companion object {
        val KEY = ID.create<String, MjClassIndexEntry>(MjClassIndex::class.java.canonicalName)
    }

    override fun getName(): ID<String, MjClassIndexEntry> = KEY

    override fun getIndexer(): DataIndexer<String, MjClassIndexEntry, FileContent> {
        return DataIndexer<String, MjClassIndexEntry, FileContent> {
            val result = mutableMapOf<String, MjClassIndexEntry>()
            val attributeValues = PsiTreeUtil.findChildrenOfType(it.psiFile, XmlAttribute::class.java)
            attributeValues.forEach { attribute ->
                if (attribute.name != "css-class" && attribute.name != "class") {
                    return@forEach
                }

                val classes = attribute.value?.split(' ') ?: return@forEach
                classes.forEach { clazz ->
                    result[clazz] =
                        MjClassIndexEntry(attribute.textOffset, TextRangeUtil.fromString(attribute.value!!, clazz))
                }
            }
            return@DataIndexer result
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): DataExternalizer<MjClassIndexEntry> {
        return object : KeyDescriptor<MjClassIndexEntry> {
            override fun getHashCode(value: MjClassIndexEntry?): Int = value.hashCode()

            override fun isEqual(val1: MjClassIndexEntry?, val2: MjClassIndexEntry?): Boolean =
                val1?.equals(val2) ?: false

            override fun save(out: DataOutput, value: MjClassIndexEntry?) {
                value ?: return

                out.writeInt(value.textOffset)
                out.writeInt(value.classTextRange.startOffset)
                out.writeInt(value.classTextRange.endOffset)
            }

            override fun read(dataInput: DataInput): MjClassIndexEntry {
                val textOffset = dataInput.readInt()
                val startOffset = dataInput.readInt()
                val endOffset = dataInput.readInt()
                return MjClassIndexEntry(textOffset, TextRange(startOffset, endOffset))
            }

        }
    }

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter {
            if (it.fileType is MjmlHtmlFileType) {
                return@InputFilter true
            }

            return@InputFilter false
        }
    }

    override fun dependsOnFileContent(): Boolean = true
}
