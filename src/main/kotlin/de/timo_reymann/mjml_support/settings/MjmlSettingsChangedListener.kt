package de.timo_reymann.mjml_support.settings

import com.intellij.util.messages.Topic

val MJML_SETTINGS_CHANGED_TOPIC = Topic.create("MjmlSettingsChangedListener", MjmlSettingsChangedListener::class.java)

/**
 * Listen for changes on mjml settings
 */
interface MjmlSettingsChangedListener {
    /**
     * Gets called when the mjml settings has been changed and the user closed the settings dialog
     */
    fun onChanged(settings: MjmlSettings)
}
