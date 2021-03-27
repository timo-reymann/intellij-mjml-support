package de.timo_reymann.mjml_support.lang

import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile

/**
 * Register custom parser definition based on html, if that is not registered
 * intellij complains
 */
class MjmlHtmlParserDefinition : HTMLParserDefinition() {
    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return MjmlFile(viewProvider)
    }
}
