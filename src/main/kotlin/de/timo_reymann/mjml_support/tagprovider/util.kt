package de.timo_reymann.mjml_support.tagprovider

val kebabCase = "(?<=[a-zA-Z])[A-Z]".toRegex()

fun String.camelToKebabCase(): String {
    return kebabCase
        .replace(this) { "-${it.value}" }
        .toLowerCase()
}
