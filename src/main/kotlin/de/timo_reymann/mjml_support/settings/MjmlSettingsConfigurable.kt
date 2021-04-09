package de.timo_reymann.mjml_support.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.*
import javax.swing.JComponent

class MjmlSettingsConfigurable(project: Project) : Configurable {
    private var state = MjmlSettings.getInstance(project)

    private val panel = panel {
        lateinit var useBuiltIn: CellBuilder<JBCheckBox>
        titledRow("Preview") {
            noteRow("Make sure to reopen preview for the changes to be detected")
            row {
                checkBox(
                    text = "Builtin rendering script",
                    prop = state::useBuiltInRenderer,
                    comment = "Disable to use custom rendering script"
                )
                    .also { useBuiltIn = it }
            }
            row {
                cell {
                    label("Path to script for rendering")
                    textFieldWithBrowseButton(
                        prop = state::renderScriptPath,
                        browseDialogTitle = "Select script",
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                    ).enableIf(useBuiltIn.selected.not())
                }
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
