package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import de.timo_reymann.mjml_support.settings.MJML_SETTINGS_CHANGED_TOPIC
import de.timo_reymann.mjml_support.settings.MjmlSettings
import de.timo_reymann.mjml_support.settings.MjmlSettingsChangedListener

object MjmlRendererServiceUtils {
    fun isJavaScriptPluginAvailable(): Boolean {
        val jsPluginId = PluginId.getId("JavaScript")
        // findEnabledPlugin returns the descriptor only when the plugin is both installed and enabled,
        // so it covers the previous null + !isDisabled checks in a single public-API call.
        return PluginManager.getInstance().findEnabledPlugin(jsPluginId) != null
    }
}

class MjmlRendererService(private val project: Project) : Disposable, MjmlSettingsChangedListener {
    private var renderer: BaseMjmlRenderer
    private val packageName = MjmlRendererService::class.java.packageName

    init {
        ApplicationManager.getApplication()
            .messageBus
            .connect(this)
            .subscribe(MJML_SETTINGS_CHANGED_TOPIC, this)

        renderer = createRenderer()
    }

    private fun shouldUseNodeRender(): Boolean =
        MjmlRendererServiceUtils.isJavaScriptPluginAvailable() &&
                MjmlSettings.getInstance(project).rendererBackend == "node"

    fun getRenderer(): BaseMjmlRenderer = renderer

    fun createRenderer(): BaseMjmlRenderer {
        val className = if (shouldUseNodeRender()) {
            "NodeMjmlRenderer"
        } else {
            "WasiMjmlRenderer"
        }
        return Class
            .forName("$packageName.$className")
            .getDeclaredConstructor(Project::class.java)
            .newInstance(project) as BaseMjmlRenderer
    }

    override fun dispose() {}

    override fun onChanged(settings: MjmlSettings) {
        // Update renderer instance
        renderer = createRenderer()

        // Force rerender with new backend
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(MJML_PREVIEW_FORCE_RENDER_TOPIC)
            .onForcedRender()
    }

    companion object {
        fun getInstance(project: Project) = project.service<MjmlRendererService>()
    }
}
