package de.timo_reymann.mjml_support.model

data class MjmlAttributeInformation(
    val name: String,
    val type: MjmlAttributeType,
    val description: String,
    val defaultValue : String? = null
)
