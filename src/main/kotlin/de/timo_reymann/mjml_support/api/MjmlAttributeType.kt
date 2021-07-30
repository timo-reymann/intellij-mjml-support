package de.timo_reymann.mjml_support.api

/**
 * Representation of well known mjml types
 */
enum class MjmlAttributeType(val description: String) {
    /**
     * Local file path
     */
    PATH("file path"),

    /**
     * Any valid web url (including tel etc.)
     */
    URL("url"),

    /**
     * Simple value, can be anything
     */
    STRING("string"),

    /**
     * Too complex to provide reliable auto complete, might be inferred in a later version
     */
    COMPLEX("custom complex format"),

    /**
     * Single pixel attribute with optional suffix
     */
    PIXEL("amount in pixels"),

    /**
     * Color specification
     */
    COLOR("color code in hex, rgba or color name"),

    CLASS("mjml or css class list"),

    /**
     * Boolean attribute
     */
    BOOLEAN("contains true or false");

    companion object {
        fun fromMjmlSpec(spec: String): MjmlAttributeType {
            return when (spec) {
                "color" -> COLOR
                "unit(px)" -> PIXEL
                "boolean" -> BOOLEAN
                else -> STRING
            }
        }
    }
}
