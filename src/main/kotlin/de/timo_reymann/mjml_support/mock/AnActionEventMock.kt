package de.timo_reymann.mjml_support.mock

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext

/**
 * Create an empty AnActionEvent
 */
object AnActionEventMock {
    fun create(): AnActionEvent {
        return AnActionEvent.createEvent(
            SimpleDataContext.builder().build(),
            Presentation(),
            "",
            ActionUiKind.TOOLBAR,
            null,
        )
    }
}
