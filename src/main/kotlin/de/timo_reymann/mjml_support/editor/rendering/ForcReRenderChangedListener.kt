package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.util.messages.Topic


val MJML_PREVIEW_FORCE_RENDER_TOPIC = Topic.create("MjmlSettingsForceRender", MjmlForceRenderListener::class.java)

/**
 * Listen for changes on mjml settings
 */
interface MjmlForceRenderListener {
    /**
     * Gets called when a rerender has been forced
     */
    fun onForcedRender()
}
