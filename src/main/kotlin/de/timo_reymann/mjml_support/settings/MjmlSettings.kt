package de.timo_reymann.mjml_support.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(name = "de.timo_reymann.mjml_support.settings.MjmlSettings", storages = [Storage("mjmlSettings.xml")])
class MjmlSettings : PersistentStateComponent<MjmlSettings>, BaseState() {
    var renderScriptPath: String by nonNullString()
    var useBuiltInRenderer: Boolean by property(true)

    private fun nonNullString(initialValue: String = "") = property(initialValue) { it == initialValue }
    override fun getState(): MjmlSettings = this
    override fun loadState(state: MjmlSettings) {
        copyFrom(state)
    }

    companion object {
        fun getInstance(project: Project) = project.service<MjmlSettings>()
    }

}
