package de.timo_reymann.mjml_support.lang.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HtmlParsing

class MjmlHtmlParser : HTMLParser() {
    override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
        return MjmlHtmlParsing(builder)
    }
}
