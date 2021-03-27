package de.timo_reymann.mjml_support.model

enum class MjmlAttributeType(val description : String) {
    PATH("file path"),
    URL("url"),
    STRING("string"),
    COMPLEX("complex type that is not specified"),
    PIXEL("amount in pixels"),
    COLOR("color code in hex, rgba or color name"),
}
