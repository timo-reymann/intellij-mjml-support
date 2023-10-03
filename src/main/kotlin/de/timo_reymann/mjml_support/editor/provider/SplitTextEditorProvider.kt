package de.timo_reymann.mjml_support.editor.provider

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.progress.runWithModalProgressBlocking
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import de.timo_reymann.mjml_support.bundle.MjmlBundle
import de.timo_reymann.mjml_support.editor.ui.MjmlFileEditorState
import org.jdom.Element

abstract class SplitTextEditorProvider(
    private val myFirstProvider: FileEditorProvider, private val mySecondProvider: FileEditorProvider
) : AsyncFileEditorProvider, DumbAware {
    private val myEditorTypeId: String =
        "split-provider[" + myFirstProvider.editorTypeId + ";" + mySecondProvider.editorTypeId + "]"

    override fun accept(project: Project, file: VirtualFile): Boolean =
        myFirstProvider.accept(project, file) && mySecondProvider.accept(project, file)

    override fun createEditor(project: Project, file: VirtualFile): FileEditor =
        createEditorAsync(project, file).build()

    override fun getEditorTypeId(): String = myEditorTypeId

    override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
        val firstBuilder = getBuilderFromEditorProvider(myFirstProvider, project, file)
        val secondBuilder = getBuilderFromEditorProvider(mySecondProvider, project, file)
        return object : AsyncFileEditorProvider.Builder() {
            override fun build(): FileEditor {
                return createSplitEditor(firstBuilder.build(), secondBuilder.build())
            }
        }
    }

    override fun readState(sourceElement: Element, project: Project, file: VirtualFile): FileEditorState {
        var child = sourceElement.getChild(FIRST_EDITOR)
        var firstState: FileEditorState? = null

        if (child != null) {
            firstState = myFirstProvider.readState(child, project, file)
        }
        child = sourceElement.getChild(SECOND_EDITOR)

        var secondState: FileEditorState? = null
        if (child != null) {
            secondState = mySecondProvider.readState(child, project, file)
        }

        val attribute = sourceElement.getAttribute(SPLIT_LAYOUT)
        val layoutName: String = attribute?.value ?: TextEditorWithPreview.Layout.SHOW_EDITOR.name

        val layout = try {
            TextEditorWithPreview.Layout.valueOf(layoutName.uppercase())
        } catch (e: Exception) {
            TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW
        }

        return MjmlFileEditorState(layout, firstState, secondState)
    }

    override fun writeState(state: FileEditorState, project: Project, targetElement: Element) {
        if (state !is MjmlFileEditorState) {
            return
        }

        var child = Element(FIRST_EDITOR)
        if (state.firstState != null) {
            myFirstProvider.writeState(state.firstState, project, child)
            targetElement.addContent(child)
        }

        child = Element(SECOND_EDITOR)
        if (state.secondState != null) {
            mySecondProvider.writeState(state.secondState, project, child)
            targetElement.addContent(child)
        }

        targetElement.setAttribute(SPLIT_LAYOUT, state.splitLayout.name)
    }

    protected abstract fun createSplitEditor(firstEditor: FileEditor, secondEditor: FileEditor): FileEditor

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

    companion object {
        private const val FIRST_EDITOR = "first_editor"
        private const val SECOND_EDITOR = "second_editor"
        private const val SPLIT_LAYOUT = "split_layout"

        private fun runsInProjectView() = !ApplicationManager.getApplication().isDispatchThread
        private fun runsInEDT() = ApplicationManager.getApplication().isWriteAccessAllowed

        fun getBuilderFromEditorProvider(
            provider: FileEditorProvider, project: Project, file: VirtualFile
        ): AsyncFileEditorProvider.Builder {
            // in case the provider doesn't provide async functionality -> wrap and create editor
            if (provider !is AsyncFileEditorProvider) {
                return object : AsyncFileEditorProvider.Builder() {
                    override fun build(): FileEditor {
                        return provider.createEditor(project, file)
                    }
                }
            }

            // called with write context
            if (runsInProjectView() || runsInEDT()) {
                return runBlockingMaybeCancellable {
                    provider.createEditorBuilder(project, file)
                }
            }

            // called from a context without write lock
            return runWithModalProgressBlocking(
                project, MjmlBundle.message("mjml_preview.opening_editor", file.name)
            ) {
                provider.createEditorBuilder(project, file)
            }
        }
    }
}
