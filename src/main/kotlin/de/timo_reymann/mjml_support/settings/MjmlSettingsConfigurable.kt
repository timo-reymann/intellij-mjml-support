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
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.dsl.builder.*
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.rendering.BuiltinRenderResourceProvider
import de.timo_reymann.mjml_support.editor.rendering.MjmlPreviewStartupActivity
import de.timo_reymann.mjml_support.editor.rendering.MjmlRendererServiceUtils
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
    private lateinit var nodeRenderercomboBox: ComboBox<String>
    private lateinit var wasiRenderercomboBox: ComboBox<String>
    private var rendererChanged: Boolean = false
    private val nodeScriptBrowseExtension = ExtendableTextComponent.Extension.create(
        AllIcons.General.OpenDisk,
        AllIcons.General.OpenDiskHover,
        MjmlBundle.message("settings.select_rendering_script_dialog.title")
    ) {
        val result = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.singleFile(), project, null)
            .choose(project)
        if (result.isEmpty()) {
            return@create
        }

        nodeRenderercomboBox.selectedItem = result[0].toNioPath().toString()
        setComboBoxModelRenderer(nodeRenderercomboBox, nodeRenderercomboBox.selectedItem as String)
    }
    private val wasiDialogBrowseExtension = ExtendableTextComponent.Extension.create(
        AllIcons.General.OpenDisk,
        AllIcons.General.OpenDiskHover,
        MjmlBundle.message("settings.select_wasi_binary_dialog.title")
    ) {
        val result = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.singleFile(), project, null)
            .choose(project)
        if (result.isEmpty()) {
            return@create
        }

        wasiRenderercomboBox.selectedItem = result[0].toNioPath().toString()
        setComboBoxModelRenderer(wasiRenderercomboBox, wasiRenderercomboBox.selectedItem as String)
    }

    private fun setComboBoxModelRenderer(comboBox: ComboBox<String>, rendererPath: String?) {
        val options: List<String> = if (rendererPath == null || rendererPath.isBlank()) {
            listOf(MjmlSettings.BUILT_IN)
        } else {
            listOf(rendererPath, MjmlSettings.BUILT_IN)
        }
        comboBox.model = CollectionComboBoxModel(options)
    }

    private val panel = panel {
        group(MjmlBundle.message("settings.group.rendering_preprocessing")) {
            row {
                checkBox(MjmlBundle.message("settings.resolve_local_images.text"))
                    .bindSelected(state::resolveLocalImages)
                    .comment(MjmlBundle.message("settings.resolve_local_images.help"))
            }.layout(RowLayout.PARENT_GRID)
            row {
                checkBox(MjmlBundle.message("settings.skip_mjml_validation.text"))
                    .bindSelected(state::skipMjmlValidation)
                    .comment(MjmlBundle.message("settings.skip_mjml_validation.help"))
            }.layout(RowLayout.PARENT_GRID)
            row {
                checkBox(MjmlBundle.message("settings.render_partials.text"))
                    .bindSelected(state::tryWrapMjmlFragment)
                    .comment(MjmlBundle.message("settings.render_partials.help"))
            }.layout(RowLayout.PARENT_GRID)
        }
        group(MjmlBundle.message("settings.group.render_backend")) {
            row {
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()) { file ->
                    file.toNioPath().toString()
                }
                    .bindText(state::mjmlConfigFile)
                    .gap(RightGap.COLUMNS)
                    .align(Align.FILL)
                    .label(MjmlBundle.message("settings.config_file.text"))
                    .comment(MjmlBundle.message("settings.config_file.help"))
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox(MutableCollectionComboBoxModel<String>(), null)
                    .label(MjmlBundle.message("settings.node_script.text"))
                    .gap(RightGap.COLUMNS)
                    .align(Align.FILL)
                    .onChanged {
                        rendererChanged = true
                        state.renderScriptPath = nodeRenderercomboBox.selectedItem?.toString() ?: ""
                    }
                    .columns(COLUMNS_MEDIUM)
                    .also {
                        nodeRenderercomboBox = it.component

                        if (state.useBuiltInNodeRenderer) {
                            setComboBoxModelRenderer(nodeRenderercomboBox, null)
                        } else {
                            setComboBoxModelRenderer(nodeRenderercomboBox, state::renderScriptPath.get())
                        }

                        //comboBox.preferredSize = Dimension(400, comboBox.preferredSize.height)
                        nodeRenderercomboBox.isEditable = true
                        nodeRenderercomboBox.editor = object : BasicComboBoxEditor() {
                            override fun createEditorComponent(): JTextField {
                                val ecbEditor = ExtendableTextField()
                                with(ecbEditor) {
                                    addExtension(nodeScriptBrowseExtension)
                                    border = null
                                }
                                return ecbEditor
                            }
                        }
                    }
                    .comment(
                        MjmlBundle.message(
                            "settings.node_script.help",
                            BuiltinRenderResourceProvider.getBundledMjmlVersion()
                        )
                    )
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox(MutableCollectionComboBoxModel<String>(), null)
                    .label(MjmlBundle.message("settings.wasi_binary.text"))
                    .gap(RightGap.COLUMNS)
                    .align(Align.FILL)
                    .onChanged {
                        rendererChanged = true
                        state.rendererWASIPath = wasiRenderercomboBox.selectedItem?.toString() ?: ""
                    }
                    .columns(COLUMNS_MEDIUM)
                    .also {
                        wasiRenderercomboBox = it.component

                        if (state.useBuiltinWASIRenderer) {
                            setComboBoxModelRenderer(wasiRenderercomboBox, null)
                        } else {
                            setComboBoxModelRenderer(wasiRenderercomboBox, state::rendererWASIPath.get())
                        }

                        //comboBox.preferredSize = Dimension(400, comboBox.preferredSize.height)
                        wasiRenderercomboBox.isEditable = true
                        wasiRenderercomboBox.editor = object : BasicComboBoxEditor() {
                            override fun createEditorComponent(): JTextField {
                                val ecbEditor = ExtendableTextField()
                                with(ecbEditor) {
                                    addExtension(wasiDialogBrowseExtension)
                                    border = null
                                }
                                return ecbEditor
                            }
                        }
                    }
                    .comment(
                        MjmlBundle.message(
                            "setting.wasi_binary.help",
                            BuiltinRenderResourceProvider.getBundledMrmlVersion()
                        )
                    )
            }.layout(RowLayout.PARENT_GRID)
            buttonsGroup(MjmlBundle.message("settings.render_backend_select.text"), true) {
                row {
                    radioButton(MjmlBundle.message("settings.render_backend_select.node.text"), "node")
                        .comment(MjmlBundle.message("settings.render_backend.select.node.help"))
                }
                row {
                    radioButton(MjmlBundle.message("settings.render_backend.select.wasi.text"), "wasi")
                        .comment(MjmlBundle.message("settings.render_backend.select.wasi.help"))
                }
            }
                .bind(state::rendererBackend)
                .visible(MjmlRendererServiceUtils.isJavaScriptPluginAvailable())
        }


        collapsibleGroup(MjmlBundle.message("settings.group_troubleshooting")) {
            row {
                button(MjmlBundle.message("settings.troubleshooting_open_folder")) {
                    Desktop.getDesktop().open(FilePluginUtil.getFile("."))
                }

                button(MjmlBundle.message("settings.troubleshooting.copy_resources")) { e ->
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

    override fun createComponent(): JComponent = panel
    override fun isModified(): Boolean = panel.isModified() || rendererChanged
    override fun reset() = panel.reset()
    override fun getDisplayName(): String = "MJML Settings"
    override fun dispose() {
        // not used
    }

    override fun apply() {
        val renderingScriptPath = nodeRenderercomboBox.selectedItem as String

        if (renderingScriptPath.trim() == "") {
            throw ConfigurationException("Custom rendering script can not be blank")
        }

        if (renderingScriptPath != MjmlSettings.BUILT_IN) {
            val renderingScriptPathFile = File(renderingScriptPath)

            if (!renderingScriptPathFile.exists()) {
                throw ConfigurationException("Custom rendering script does not exist")
            }

            // Make sure path is OS indecent so on Windows path variables are replaced properly
            nodeRenderercomboBox.selectedItem = FileUtil.toSystemIndependentName(renderingScriptPath)
        }

        // Validate WASI only if it is possible to configure
        if (MjmlRendererServiceUtils.isJavaScriptPluginAvailable()) {
            val wasiBinaryPath = wasiRenderercomboBox.selectedItem as String
            if (wasiBinaryPath.trim() == "") {
                throw ConfigurationException("Custom WASI binary can not be blank")
            }

            if (wasiBinaryPath != MjmlSettings.BUILT_IN) {
                val wasiPathFile = File(wasiBinaryPath)

                if (!wasiPathFile.exists()) {
                    throw ConfigurationException("Custom WASI bianry does not exist")
                }

                // Make sure path is OS indecent so on Windows path variables are replaced properly
                wasiRenderercomboBox.selectedItem = FileUtil.toSystemIndependentName(renderingScriptPath)
            }
        }

        panel.apply()

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_SETTINGS_CHANGED_TOPIC)
            .onChanged(this.state)
    }
}
