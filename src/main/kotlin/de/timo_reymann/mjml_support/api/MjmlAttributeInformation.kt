package de.timo_reymann.mjml_support.api

data class MjmlAttributeInformation(
    /**
     * Name of the attribute
     */
    val name: String,
    val type: MjmlAttributeType,

    /**
     * Description shown in quick help
     */
    val description: String,

    /**
     * Default value of attribute, if is none omit or set explicitly to null
     */
    val defaultValue: String? = null
)
