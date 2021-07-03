package de.timo_reymann.mjml_support.settings

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.not
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import de.timo_reymann.mjml_support.editor.MjmlPreviewStartupActivity
import de.timo_reymann.mjml_support.editor.render.BuiltinRenderResourceProvider
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.util.UiTimerUtil
import java.awt.Desktop
import java.io.File
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.event.DocumentEvent

class MjmlSettingsConfigurable(project: Project) : Configurable, Disposable {
    private var state = MjmlSettings.getInstance(project)
    private lateinit var renderingScriptTextField: JTextField;

    private val panel = panel {
        lateinit var useBuiltIn: CellBuilder<JBCheckBox>
        titledRow("Preview") {
            row {
                checkBox(
                    text = "Builtin rendering script (MJML v" + BuiltinRenderResourceProvider.getBundledMjmlVersion() + ")",
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
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(
                            JavaScriptFileType.INSTANCE
                        )
                    ).enableIf(useBuiltIn.selected.not())
                        .comment("The selected script will be executed with Node.JS")
                        .also { renderingScriptTextField = it.component.textField }
                }
            }
        }

        titledRow("Trouble Shooting") {
            row {
                cell {
                    button("Open plugin folder") {
                        Desktop.getDesktop().open(FilePluginUtil.getFile("."))
                    }

                    button("Copy files for preview from plugin") { e ->
                        MjmlPreviewStartupActivity().runActivity(project)
                        val button = e.source as JButton
                        button.isEnabled = false
                        UiTimerUtil.singleExecutionAfter(2) {
                            button.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun isValidScript(path: String): Boolean = File(path).exists()
    override fun createComponent(): JComponent = panel
    override fun isModified(): Boolean = panel.isModified()
    override fun reset() = panel.reset()
    override fun getDisplayName(): String = "MJML Settings"
    override fun dispose() {}

    override fun apply() {
        val renderingScriptPath = renderingScriptTextField.text
        if (renderingScriptPath.trim() != "" && !File(renderingScriptPath).exists()) {
            throw ConfigurationException("Custom rendering script does not exist")
        }

        panel.apply()

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_SETTINGS_CHANGED_TOPIC)
            .onChanged(this.state)
    }
}
