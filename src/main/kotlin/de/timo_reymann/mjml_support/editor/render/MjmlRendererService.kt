package de.timo_reymann.mjml_support.editor.render

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object MjmlRendererServiceUtils {
    fun isJavaScriptPluginAvailable(): Boolean {
        val jsPluginId = PluginId.getId("JavaScript")
        val plugin = PluginManagerCore.getPlugin(jsPluginId)
        return plugin != null && !PluginManagerCore.isDisabled(jsPluginId)
    }
}

class MjmlRendererService(private val project: Project) {
    fun getRenderer(virtualFile: VirtualFile): BaseMjmlRenderer {
        return MjmlRenderer(project, virtualFile)
    }

    companion object {
        fun getInstance(project: Project) = project.service<MjmlRendererService>()
    }
}
