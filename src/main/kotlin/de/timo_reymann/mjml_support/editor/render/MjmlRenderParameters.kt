package de.timo_reymann.mjml_support.editor.render

data class MjmlRenderParameters(
    var directory: String,
    var content: String,
    var options: MjmlRenderParametersOptions,
    var filePath: String
)

data class MjmlRenderParametersOptions(
    var mjmlConfigPath: String?
)
