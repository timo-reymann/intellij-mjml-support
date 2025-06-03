package de.timo_reymann.mjml_support.editor.rendering


data class MjmlRenderParameters(
    var directory: String,
    var content: String,
    var options: MjmlRenderParametersOptions,
    var filePath: String
)

data class MjmlRenderParametersOptions(
    var mjmlConfigPath: String?
)


class MjmlRenderResult {
    var html: String? = null
    var stdout: String? = null
    lateinit var errors: Array<MjmlRenderResultError>
}

class MjmlRenderResultError {
    var line: Int? = null
    var message: String? = null
    var tagName: String? = null
    var formattedMessage: String? = null

    constructor(){ }

    constructor(message: String) {
        this.message = message
    }
}
