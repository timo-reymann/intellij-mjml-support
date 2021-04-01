package de.timo_reymann.mjml_support.editor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class MjmlRenderResult {
    public lateinit var html: String
}
