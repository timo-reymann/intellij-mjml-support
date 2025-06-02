package de.timo_reymann.mjml_support.editor.render

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import de.timo_reymann.mjml_support.settings.MjmlSettings

object MjmlRendererServiceUtils {
    fun isJavaScriptPluginAvailable(): Boolean {
        val jsPluginId = PluginId.getId("JavaScript")
        val plugin = PluginManagerCore.getPlugin(jsPluginId)
        return plugin != null && !PluginManagerCore.isDisabled(jsPluginId)
    }
}

@Service(Service.Level.PROJECT)
class MjmlRendererService(private val project: Project) {
    private fun shouldUseNodeRender(): Boolean {
        return MjmlRendererServiceUtils.isJavaScriptPluginAvailable() &&
                MjmlSettings.getInstance(project).rendererBackend == "node"
    }

    // TODO Rewrite to use shared instance and update whenever settings change is triggered
    fun getRenderer(virtualFile: VirtualFile): BaseMjmlRenderer {
        if (shouldUseNodeRender()) {
            return NodeMjmlRenderer(project, virtualFile)
        } else {
            return WasiMjmlRenderer(project, virtualFile)
        }
    }

    companion object {
        fun getInstance(project: Project) = project.service<MjmlRendererService>()
    }
}
