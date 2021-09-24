package de.timo_reymann.mjml_support.editor.ui

import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.TextEditorWithPreview

class MjmlFileEditorState(val splitLayout: TextEditorWithPreview.Layout, val firstState: FileEditorState?, val secondState: FileEditorState?) :
    FileEditorState {

    override fun canBeMergedWith(otherState: FileEditorState, level: FileEditorStateLevel): Boolean {
        return (otherState is MjmlFileEditorState
                && (firstState == null || firstState.canBeMergedWith(otherState.firstState!!, level))
                && (secondState == null || secondState.canBeMergedWith(otherState.secondState!!, level)))
    }
}
