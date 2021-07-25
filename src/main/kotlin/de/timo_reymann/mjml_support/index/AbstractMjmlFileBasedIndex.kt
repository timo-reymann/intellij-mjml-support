package de.timo_reymann.mjml_support.index

import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

abstract class AbstractMjmlFileBasedIndex<T>(private val indexVersion : Int) : FileBasedIndexExtension<String, T>() {
    private val inputFilter = FileBasedIndex.InputFilter {
        if (it.fileType is MjmlHtmlFileType) {
            return@InputFilter true
        }

        return@InputFilter false
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter = inputFilter

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun dependsOnFileContent(): Boolean = true

    override fun getVersion(): Int = indexVersion
}
