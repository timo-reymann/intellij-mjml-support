package de.timo_reymann.mjml_support.api

data class MjmlAttributeInformation(
    /**
     * Name of the attribute
     */
    val name: String,

    /**
     * Type of the attribute
     */
    val type: MjmlAttributeType,

    /**
     * Description shown in quick help
     */
    val description: String,

    /**
     * Default value of attribute, if is none omit or set explicitly to null
     */
    var defaultValue: String? = null
)
