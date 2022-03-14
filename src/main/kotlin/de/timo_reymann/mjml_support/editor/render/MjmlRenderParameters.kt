package de.timo_reymann.mjml_support.editor.render

data class MjmlRenderParameters(
    var directory: String,
    var content: String,
    var options: MjmlRenderParametersOptions
)

data class MjmlRenderParametersOptions(
    var mjmlConfigPath: String?
)
