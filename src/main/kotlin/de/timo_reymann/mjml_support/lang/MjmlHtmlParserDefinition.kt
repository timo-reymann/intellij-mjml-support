package de.timo_reymann.mjml_support.lang

import com.intellij.lang.PsiParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.lexer.XmlLexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.parsing.xml.XmlParser

/**
 * Register custom parser definition based on html, if that is not registered
 * intellij complains
 */
class MjmlHtmlParserDefinition : HTMLParserDefinition() {
    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return MjmlFile(viewProvider)
    }

    override fun createParser(project: Project?): PsiParser {
        return XmlParser()
    }
}
