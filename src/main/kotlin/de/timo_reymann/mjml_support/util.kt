package de.timo_reymann.mjml_support

import com.intellij.psi.PsiElement
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage

fun isInMjmlFile(context: PsiElement) = context.containingFile.language is MjmlHtmlLanguage
