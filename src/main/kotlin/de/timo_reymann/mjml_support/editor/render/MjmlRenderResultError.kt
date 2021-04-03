package de.timo_reymann.mjml_support.editor.render

class MjmlRenderResultError {
    var line: Int? = null
    var message: String? = null
    var tagName: String? = null
    var formattedMessage: String? = null

    constructor(message : String) {
        this.message = message
    }
}
