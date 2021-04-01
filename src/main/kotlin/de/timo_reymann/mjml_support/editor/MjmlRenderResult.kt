package de.timo_reymann.mjml_support.editor

// @JsonIgnoreProperties(ignoreUnknown = true)
class MjmlRenderResult {
    var html: String? = null
    lateinit var errors: Array<MjmlRenderResultError>
}

class MjmlRenderResultError {
    var line: Int? = null
    var message: String? = null
    var tagName: String? = null
    var formattedMessage: String? = null

    constructor(){}

    constructor(message : String) {
        this.message = message
    }
}
