package de.timo_reymann.mjml_support.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class MjmlSettingsConfigurable(project: Project) : Configurable {
    private var state = MjmlSettings.getInstance(project)

    private val panel = panel {
        row {
            cell {
                label("Path to script for rendering")
                textFieldWithBrowseButton(
                    prop = state::renderScriptPath,
                    browseDialogTitle = "Select script",
                    fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                )
            }
        }
    }

    override fun createComponent(): JComponent = panel
    override fun isModified(): Boolean = panel.isModified()

    override fun apply() {
        panel.apply()
    }

    override fun reset() {
        panel.reset()
    }

    override fun getDisplayName(): String = "MJML Settings"

}
