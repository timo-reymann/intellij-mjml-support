package de.timo_reymann.mjml_support.model

enum class MjmlAttributeType(val description : String) {
    PATH("file path"),
    URL("url"),
    STRING("string"),
    COMPLEX("custom complex format"),
    PIXEL("amount in pixels"),
    COLOR("color code in hex, rgba or color name"),
}
