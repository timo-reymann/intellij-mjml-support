package de.timo_reymann.mjml_support.index

import com.intellij.openapi.util.TextRange

enum class MjmlClassDefinitionType {
    MJML_CLASS, MJ_STYLE
}

data class MjmlClassDefinition(
    val textOffset : Int,
    val textRange : TextRange,
    val type : MjmlClassDefinitionType
)
