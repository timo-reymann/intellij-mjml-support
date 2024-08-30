package de.timo_reymann.mjml_support.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Entities

object HtmlUtil {
    fun escape(htmlText: String): String {
        val doc = Jsoup.parse(htmlText)
        doc.outputSettings().escapeMode(Entities.EscapeMode.base)
        doc.outputSettings().charset("ASCII")
        return doc.html()
    }
}
