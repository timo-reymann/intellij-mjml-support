package de.timo_reymann.mjml_support.lang.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import org.jetbrains.annotations.NotNull

class MjmlHtmlParsing(builder : PsiBuilder) : HtmlParsing(builder) {
    /**
     * Set html tag element type to xml tag to prevent html attributes for autocomplete
     */
    override fun getHtmlTagElementType(info: HtmlTagInfo, tagLevel: Int): IElementType = XmlElementType.XML_TAG
}
