package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import de.timo_reymann.mjml_support.editor.render.BuiltinRenderResourceProvider

/**
 * Activity to copy editor files over
 */
class MjmlPreviewStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, "Copy renderer files for mjml preview") {
                override fun run(indicator: ProgressIndicator) = BuiltinRenderResourceProvider.copyResources()
            })
    }
}
