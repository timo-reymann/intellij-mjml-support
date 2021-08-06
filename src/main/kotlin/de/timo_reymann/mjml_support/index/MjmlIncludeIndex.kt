package de.timo_reymann.mjml_support.index

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import java.io.DataInput
import java.io.DataOutput

class MjmlIncludeIndex : AbstractMjmlFileBasedIndex<MjmlIncludeInfo>(1) {
    companion object {
        val KEY = ID.create<String, MjmlIncludeInfo>(MjmlIncludeIndex::class.java.canonicalName)

        fun createIndexKey(file: VirtualFile): String {
            var targetFile = file

            if (file is VirtualFileWindow) {
                targetFile = (file as VirtualFileWindow).delegate
            }

            return try {
                targetFile.toNioPath().toString()
            } catch (e: UnsupportedOperationException) {
                targetFile.path
            }
        }
    }

    private val indexer = DataIndexer<String, MjmlIncludeInfo, FileContent> { fileContent ->
        val result = mutableMapOf<String, MjmlIncludeInfo>()
        PsiTreeUtil.findChildrenOfType(fileContent.psiFile, XmlTag::class.java)
            .filter { it.name == "mj-include" }
            .forEach {
                val includePath = it.getAttributeValue("path") ?: return@forEach
                val includedFile = VfsUtil.findRelativeFile(includePath, fileContent.file) ?: return@forEach
                val type = it.getAttribute("type")?.value ?: "mjml"
                val key: String

                // In case the underlying file system doesnt support NIO Paths
                // e.g. testing with TempFileSystem
                try {
                    key = includedFile.toNioPath().toString()
                } catch (e: UnsupportedOperationException) {
                    return@forEach
                }

                if (result.containsKey(key)) {
                    result[key] = MjmlIncludeInfo(result[key]!!.occurrences + 1, type)
                } else {
                    result[key] = MjmlIncludeInfo(1, type)
                }
            }

        return@DataIndexer result
    }

    private val valueExternalizer = object : DataExternalizer<MjmlIncludeInfo> {
        override fun save(out: DataOutput, value: MjmlIncludeInfo?) {
            value ?: return

            out.writeInt(value.occurrences)
            out.writeUTF(value.type)
        }

        override fun read(dataInput: DataInput): MjmlIncludeInfo {
            val occurrences = dataInput.readInt()
            val type = dataInput.readUTF()
            return MjmlIncludeInfo(occurrences, type)
        }

    }

    override fun getName(): ID<String, MjmlIncludeInfo> = KEY

    override fun getIndexer(): DataIndexer<String, MjmlIncludeInfo, FileContent> = indexer

    override fun getValueExternalizer(): DataExternalizer<MjmlIncludeInfo> = valueExternalizer
}
