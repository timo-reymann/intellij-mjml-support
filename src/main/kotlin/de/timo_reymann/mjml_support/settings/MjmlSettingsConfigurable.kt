package de.timo_reymann.mjml_support.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
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
import de.timo_reymann.mjml_support.editor.render.BuiltinRenderResourceProvider
import de.timo_reymann.mjml_support.editor.render.MjmlPreviewStartupActivity
import de.timo_reymann.mjml_support.editor.render.MjmlRendererServiceUtils
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
        "Select rendering script"
    ) {
        val result = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor(), project, null)
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
        "Select WASI binary"
    ) {
        val result = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor(), project, null)
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
        group("Rendering Preprocessing") {
            row {
                checkBox("Resolve local image paths")
                    .bindSelected(state::resolveLocalImages)
                    .comment(
                        "While mails can not use local paths, the plugin can resolve them for you in the preview. " +
                                "If you use a lot of local images this might impact preview update performance!"
                    )
            }.layout(RowLayout.PARENT_GRID)
            row {
                checkBox("Skip MJML validation")
                    .bindSelected(state::skipMjmlValidation)
                    .comment(
                        "Do not validate if MJML files contain a root tag and body. " +
                                "This allows you to use custom rendering scripts that do this on their own."
                    )
            }.layout(RowLayout.PARENT_GRID)
            row {
                checkBox("Try rendering partial MJML")
                    .bindSelected(state::tryWrapMjmlFragment)
                    .comment(
                        "Enable this option so the plugin tries to wrap mjml files without a proper " +
                                "structure in a mjml skeleton and sends them to the render script."
                    )
            }.layout(RowLayout.PARENT_GRID)
        }
        group("Rendering Backend") {
            row {
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()) { file ->
                    file.toNioPath().toString()
                }
                    .bindText(state::mjmlConfigFile)
                    .gap(RightGap.COLUMNS)
                    .align(Align.FILL)
                    .label("Config file")
                    .comment("Path or directory of .mjmlconfig file (leave blank for default, will search in same folder as the mjml file)")
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox(MutableCollectionComboBoxModel<String>(), null)
                    .label("Node.js script")
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
                    .comment("""Bundled script uses MJML v${BuiltinRenderResourceProvider.getBundledMjmlVersion()}, For more information click <a href="https://plugins.jetbrains.com/plugin/16418-mjml-support/tutorials/custom-rendering-script">here</a>.""")
            }.layout(RowLayout.PARENT_GRID)
            row {
                comboBox(MutableCollectionComboBoxModel<String>(), null)
                    .label("WASI binary")
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
                            setComboBoxModelRenderer(wasiRenderercomboBox,null)
                        } else {
                            setComboBoxModelRenderer(wasiRenderercomboBox,state::rendererWASIPath.get())
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
                    .comment("""Bundled WASI uses MRML v${BuiltinRenderResourceProvider.getBundledMjmlVersion()}, For more information click <a href="https://plugins.jetbrains.com/plugin/16418-mjml-support/tutorials/custom-rendering-script">here</a>.""")
            }.layout(RowLayout.PARENT_GRID)
            buttonsGroup("Renderer backend to use", true) {
                row {
                    radioButton("Node.js", "node").comment("Only available in IDEs with JavaScript plugin")
                }
                row {
                    radioButton("WASI", "wasi").comment("Always available, faster but potentially differing output")
                }
            }
                .bind(state::rendererBackend)
                .visible(MjmlRendererServiceUtils.isJavaScriptPluginAvailable())
        }


        collapsibleGroup("Trouble Shooting") {
            row {
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
        panel.apply()

        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_SETTINGS_CHANGED_TOPIC)
            .onChanged(this.state)
    }
}
