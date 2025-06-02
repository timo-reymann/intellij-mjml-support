package de.timo_reymann.mjml_support.editor.render

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * Activity to copy editor files over
 */
class MjmlPreviewStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, "Copy renderer files for mjml preview") {
                override fun run(indicator: ProgressIndicator) = BuiltinRenderResourceProvider.copyResources()
            })
            .run { object: Task.Backgroundable(project, "Get bundled WASI version") {
                override fun run(indicator: ProgressIndicator) {
                    BuiltinRenderResourceProvider.extractMrmlVersion()
                }
            } }
    }
}
