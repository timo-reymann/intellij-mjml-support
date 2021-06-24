package de.timo_reymann.mjml_support.tagprovider

import java.util.*

val kebabCase = "(?<=[a-zA-Z])[A-Z]".toRegex()

fun String.camelToKebabCase(): String {
    return kebabCase
        .replace(this) { "-${it.value}" }
        .lowercase(Locale.getDefault())
}
