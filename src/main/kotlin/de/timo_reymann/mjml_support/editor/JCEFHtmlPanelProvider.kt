package de.timo_reymann.mjml_support.editor

import com.intellij.ui.jcef.JBCefApp

class JCEFHtmlPanelProvider : MjmlHtmlPanelProvider() {
    override fun createHtmlPanel(): MjmlJCEFHtmlPanel = MjmlJCEFHtmlPanel()

    override fun isAvailable(): AvailabilityInfo =
        when {
            JBCefApp.isSupported() -> AvailabilityInfo.AVAILABLE
            else -> AvailabilityInfo.UNAVAILABLE
        }

    override fun getProviderInfo(): ProviderInfo = ProviderInfo("JCEF Browser", JCEFHtmlPanelProvider::class.java.name)
}
