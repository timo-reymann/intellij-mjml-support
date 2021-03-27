package de.timo_reymann.mjml_support.reference

import com.intellij.lang.Language
import com.intellij.psi.css.EmbeddedCssProvider
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage

class MjmlEmbeddedCssProvider : EmbeddedCssProvider() {
    override fun enableEmbeddedCssFor(language: Language) = language is MjmlHtmlLanguage
}
