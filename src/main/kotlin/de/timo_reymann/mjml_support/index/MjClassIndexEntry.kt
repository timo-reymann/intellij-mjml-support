package de.timo_reymann.mjml_support.index

import com.intellij.openapi.util.TextRange

data class MjClassIndexEntry(
    val textOffset : Int,
    val classTextRange : TextRange
)
