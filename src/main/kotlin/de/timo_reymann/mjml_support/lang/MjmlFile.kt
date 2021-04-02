package de.timo_reymann.mjml_support.lang

import com.intellij.psi.FileViewProvider
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.tree.IFileElementType

val FILE_TYPE = IFileElementType("MJML_FILE_ELEMENT_TYPE", MjmlHtmlLanguage.INSTANCE)

class MjmlFile(viewProvider: FileViewProvider) : HtmlFileImpl(viewProvider, FILE_TYPE) {
    override fun toString(): String = "MjmlFile: $name"
}
