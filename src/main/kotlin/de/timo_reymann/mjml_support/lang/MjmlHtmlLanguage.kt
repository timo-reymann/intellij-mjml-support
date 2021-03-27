package de.timo_reymann.mjml_support.lang

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage

class MjmlHtmlLanguage : HTMLLanguage(XMLLanguage.INSTANCE, "mjml") {
    companion object {
        val INSTANCE = MjmlHtmlLanguage()
    }
}
