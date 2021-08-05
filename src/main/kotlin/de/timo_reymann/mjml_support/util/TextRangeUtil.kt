package de.timo_reymann.mjml_support.util

import com.intellij.openapi.util.TextRange

object TextRangeUtil {
    fun fromString(str: String, substring : String): TextRange {
        val start = str.indexOf(substring) + 1
        return TextRange(start, start + substring.length)
    }
}
