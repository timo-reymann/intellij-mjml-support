package de.timo_reymann.mjml_support.lang

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.XmlLikeFileType
import de.timo_reymann.mjml_support.icons.MjmlIcons

class MjmlHtmlFileType : HtmlFileType(MjmlHtmlLanguage.INSTANCE) {
    override fun getIcon() = MjmlIcons.COLORED
    override fun getName() = "MJML"
    override fun getDefaultExtension() = "mjml"
    override fun getDescription(): String = "File type for MailJet markup language"

    companion object {
        @JvmField
        val INSTANCE = MjmlHtmlFileType()
    }
}
