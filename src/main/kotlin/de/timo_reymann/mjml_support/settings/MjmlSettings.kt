package de.timo_reymann.mjml_support.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(name = "de.timo_reymann.mjml_support.settings.MjmlSettings", storages = [Storage("mjmlSettings.xml")])
class MjmlSettings : PersistentStateComponent<MjmlSettings>, BaseState() {
    var renderScriptPath: String by nonNullString(BUILT_IN)
    var resolveLocalImages by property(false)
    val useBuiltInRenderer: Boolean
        get() = renderScriptPath == BUILT_IN || renderScriptPath.isBlank()
    var skipMjmlValidation by property(false)
    var mjmlConfigFile by nonNullString("")

    private fun nonNullString(initialValue: String = "") = property(initialValue) { it == initialValue }
    override fun getState(): MjmlSettings = this
    override fun loadState(state: MjmlSettings) {
        copyFrom(state)
    }

    companion object {
        fun getInstance(project: Project) = project.service<MjmlSettings>()
        const val BUILT_IN = "Bundled"
    }

}
