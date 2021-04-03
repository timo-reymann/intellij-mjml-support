package de.timo_reymann.mjml_support.editor.render

class MjmlRenderResult {
    var html: String? = null
    lateinit var errors: Array<MjmlRenderResultError>
}
