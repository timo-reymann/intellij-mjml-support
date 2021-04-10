package de.timo_reymann.mjml_support.settings

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.not
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import java.io.File
import javax.swing.JComponent

class MjmlSettingsConfigurable(project: Project) : Configurable {
    private var state = MjmlSettings.getInstance(project)

    private val panel = panel {
        lateinit var useBuiltIn: CellBuilder<JBCheckBox>
        titledRow("Preview") {
            // noteRow("Make sure to reopen preview for the changes to be detected")
            row {
                checkBox(
                    text = "Builtin rendering script",
                    prop = state::useBuiltInRenderer,
                    comment = "Disable to use custom rendering script"
                ).also { useBuiltIn = it }
            }
            row {
                cell {
                    label("Path to custom script for rendering")
                    textFieldWithBrowseButton(
                        prop = state::renderScriptPath,
                        browseDialogTitle = "Select script",
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(JavaScriptFileType.INSTANCE)
                    ).enableIf(useBuiltIn.selected.not())
                        .comment("The selected script will be executed with Node.JS")
                }
            }
        }
    }

    override fun createComponent(): JComponent = panel
    override fun isModified(): Boolean = panel.isModified()

    override fun apply() {
        panel.apply()

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_SETTINGS_CHANGED_TOPIC)
            .onChanged(this.state)
    }

    override fun reset() {
        panel.reset()
    }

    override fun getDisplayName(): String = "MJML Settings"

}
