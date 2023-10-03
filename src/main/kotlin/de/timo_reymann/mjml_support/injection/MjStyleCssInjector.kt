package de.timo_reymann.mjml_support.injection

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.*
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage

class MjStyleCssInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        // Prevent style injection to be called outside of mjml context
        if (context.language != MjmlHtmlLanguage.INSTANCE) {
            return
        }

        val host = context as PsiLanguageInjectionHost
        when (host.parent) {
            // Highlight child content
            is XmlTag -> {
                if ((host.parent as XmlTag).name == "mj-style") {
                    highlight(registrar, host, false)
                }
            }

            // Highlight value content
            is XmlAttribute -> {
                if ((host.parent as XmlAttribute).name == "style") {
                    highlight(registrar, host, true)
                }
            }
        }
    }

    private fun highlight(registrar: MultiHostRegistrar, host: PsiLanguageInjectionHost, inline: Boolean) {
        registrar.startInjecting(CSSLanguage.INSTANCE)

        if (inline) {
            registrar.addPlace("inline.style {", "}", host, ElementManipulators.getValueTextRange(host))
        } else {
            registrar.addPlace("", "", host, ElementManipulators.getValueTextRange(host))
        }

        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> =
        listOf(XmlText::class.java, XmlAttributeValue::class.java)
}
