package de.timo_reymann.mjml_support.mock

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext

/**
 * Create an empty AnActionEvent
 */
class AnActionEventMock() : AnActionEvent( null,
     SimpleDataContext.builder().build(),
    "",
    Presentation(),
    ActionManager.getInstance(),
    0) {
}

