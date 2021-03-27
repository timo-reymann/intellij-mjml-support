package de.timo_reymann.mjml_support.completion

import com.intellij.codeInsight.completion.CompletionParameters

fun getText(parameters: CompletionParameters): String {
    val caretPositionInString = parameters.offset - parameters.position.textOffset
    var queryString = parameters.position
        .text
        .substring(0, caretPositionInString)
    if (queryString.startsWith("'") || queryString.startsWith("\"")) {
        queryString = queryString.substring(1)
    }
    return queryString
}
