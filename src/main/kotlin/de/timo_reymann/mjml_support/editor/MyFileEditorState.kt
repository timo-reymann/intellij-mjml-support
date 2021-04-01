package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel

class MyFileEditorState(val splitLayout: String?, val firstState: FileEditorState?, val secondState: FileEditorState?) :
    FileEditorState {

    override fun canBeMergedWith(otherState: FileEditorState, level: FileEditorStateLevel): Boolean {
        return (otherState is MyFileEditorState
                && (firstState == null || firstState.canBeMergedWith(otherState.firstState!!, level))
                && (secondState == null || secondState.canBeMergedWith(otherState.secondState!!, level)))
    }
}
