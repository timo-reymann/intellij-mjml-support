package de.timo_reymann.mjml_support.lang.xml

import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage

class MjmlXmlExtension : HtmlXmlExtension() {
    override fun isAvailable(file: PsiFile) = file.language == MjmlHtmlLanguage.INSTANCE

    override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean = true

    override fun isCustomTagAllowed(tag: XmlTag?): Boolean = true
}
