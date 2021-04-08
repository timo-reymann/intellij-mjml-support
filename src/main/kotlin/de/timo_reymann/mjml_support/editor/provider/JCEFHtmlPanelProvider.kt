package de.timo_reymann.mjml_support.editor.provider

import com.intellij.ui.jcef.JBCefApp
import de.timo_reymann.mjml_support.editor.MjmlHtmlPanelProvider
import de.timo_reymann.mjml_support.editor.MjmlJCEFHtmlPanel

class JCEFHtmlPanelProvider : MjmlHtmlPanelProvider() {
    override fun createHtmlPanel(): MjmlJCEFHtmlPanel = MjmlJCEFHtmlPanel()

    override fun isAvailable(): AvailabilityInfo =
        when {
            JBCefApp.isSupported() -> AvailabilityInfo.AVAILABLE
            else -> AvailabilityInfo.UNAVAILABLE
        }


    override fun getProviderInfo(): ProviderInfo = ProviderInfo("JCEF Browser", JCEFHtmlPanelProvider::class.java.name)
}
