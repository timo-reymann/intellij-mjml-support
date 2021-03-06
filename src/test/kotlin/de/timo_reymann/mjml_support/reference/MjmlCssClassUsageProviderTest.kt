package de.timo_reymann.mjml_support.reference

import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class MjmlCssClassUsageProviderTest : MjmlPluginBaseTestCase() {

    fun testUsage() {
        checkHighlighting(CssUnusedSymbolInspection())
    }

    fun testNoUsage() {
        checkHighlighting(CssUnusedSymbolInspection())
    }

    override fun getTestDataPath(): String {
        return super.getTestDataPath() + "reference/cssClass"
    }
}
