package de.timo_reymann.mjml_support.lang.parsing

import com.intellij.lang.PsiParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import de.timo_reymann.mjml_support.lang.MjmlFile

/**
 * Register custom parser definition based on html, if that is not registered
 * intellij complains
 */
class MjmlHtmlParserDefinition : HTMLParserDefinition() {
    override fun createFile(viewProvider: FileViewProvider): PsiFile = MjmlFile(viewProvider)

    /**
     * Custom parser to parse everything like html with mjml tweaks
     */
    override fun createParser(project: Project?): PsiParser = MjmlHtmlParser()
}
