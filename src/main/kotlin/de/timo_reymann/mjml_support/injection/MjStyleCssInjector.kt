package de.timo_reymann.mjml_support.injection

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.containers.ContainerUtil

class MjStyleCssInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val host = context as PsiLanguageInjectionHost
        when (host.parent) {
            is XmlTag -> {
                if ((host.parent as HtmlTag).name == "mj-style") {
                    highlight(registrar, host, false)
                }
            }

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
        ContainerUtil.immutableList(XmlText::class.java, XmlAttributeValue::class.java)
}
