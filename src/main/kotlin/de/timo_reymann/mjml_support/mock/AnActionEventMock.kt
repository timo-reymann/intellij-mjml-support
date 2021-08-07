package de.timo_reymann.mjml_support.mock

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation

/**
 * Create an empty AnActionEvent
 */
class AnActionEventMock : AnActionEvent( null,
    DataManager.getInstance().dataContext,
    "",
    Presentation(),
    ActionManager.getInstance(),
    0) {
}
