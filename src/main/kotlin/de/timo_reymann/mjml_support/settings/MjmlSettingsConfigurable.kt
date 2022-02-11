package de.timo_reymann.mjml_support.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import de.timo_reymann.mjml_support.editor.render.MjmlPreviewStartupActivity
import de.timo_reymann.mjml_support.editor.render.BuiltinRenderResourceProvider
import de.timo_reymann.mjml_support.util.FilePluginUtil
import de.timo_reymann.mjml_support.util.UiTimerUtil
import java.awt.Desktop
import java.io.File
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxEditor

class MjmlSettingsConfigurable(project: Project) : Configurable, Disposable {

    private var state = MjmlSettings.getInstance(project)
    private lateinit var comboBox: ComboBox<String>
    private val browseExtension = ExtendableTextComponent.Extension.create(
        AllIcons.General.OpenDisk, AllIcons.General.OpenDiskHover,
        "Select custom rendering script"
    ) {
        val result = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor(), project, null)
            .choose(project)
        if (result.isEmpty()) {
            return@create
        }

        comboBox.selectedItem = result[0].toNioPath().toString()
        setComboBoxModelRenderer(comboBox.selectedItem as String)
    }

    private fun setComboBoxModelRenderer(rendererScript: String?) {
        val options: List<String> = if (rendererScript == null || rendererScript.isBlank()) {
            listOf(MjmlSettings.BUILT_IN)
        } else {
            listOf(rendererScript, MjmlSettings.BUILT_IN)
        }
        comboBox.model = CollectionComboBoxModel(options)
    }

    private val panel = panel {
        titledRow("Preview") {
            row {
                cell(isFullWidth = true) {
                    checkBox("Resolve local image paths", state::resolveLocalImages)
                        .comment("While mails can not use local paths, the plugin can resolve them for you in the preview. " +
                                "If you use a lot of local images this might impact preview update performance!")
                }
            }
            row {
                cell(isFullWidth = true) {
                    label("Rendering script")
                    comboBox(CollectionComboBoxModel(), state::renderScriptPath)
                        .growPolicy(GrowPolicy.MEDIUM_TEXT)
                        .also {
                            comboBox = it.component

                            if (state.useBuiltInRenderer) {
                                setComboBoxModelRenderer(null)
                            } else {
                                setComboBoxModelRenderer(state::renderScriptPath.get())
                            }

                            //comboBox.preferredSize = Dimension(400, comboBox.preferredSize.height)
                            comboBox.isEditable = true
                            comboBox.editor = object : BasicComboBoxEditor() {
                                override fun createEditorComponent(): JTextField {
                                    val ecbEditor = ExtendableTextField()
                                    with(ecbEditor) {
                                        addExtension(browseExtension)
                                        border = null
                                    }
                                    return ecbEditor
                                }
                            }
                        }
                        .comment("""Bundled script uses MJML v${BuiltinRenderResourceProvider.getBundledMjmlVersion()}, For more information about custom rendering scripts click <a href="https://plugins.jetbrains.com/plugin/16418-mjml-support/tutorials/custom-rendering-script">here</a>.""")
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
                        with(e.source as JButton) {
                            isEnabled = false

                            UiTimerUtil.singleExecutionAfter(2) {
                                this.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun createComponent(): JComponent = panel
    override fun isModified(): Boolean = panel.isModified()
    override fun reset() = panel.reset()
    override fun getDisplayName(): String = "MJML Settings"
    override fun dispose() {}

    override fun apply() {
        val renderingScriptPath = comboBox.selectedItem as String

        if (renderingScriptPath.trim() != "" && !File(renderingScriptPath).exists() && renderingScriptPath != MjmlSettings.BUILT_IN) {
            throw ConfigurationException("Custom rendering script does not exist")
        }

        panel.apply()

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_SETTINGS_CHANGED_TOPIC)
            .onChanged(this.state)
    }
}
