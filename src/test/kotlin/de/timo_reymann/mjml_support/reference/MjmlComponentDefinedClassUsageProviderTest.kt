package de.timo_reymann.mjml_support.reference

import com.intellij.psi.css.inspections.CssUnusedSymbolInspection
import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class MjmlComponentDefinedClassUsageProviderTest : MjmlPluginBaseTestCase() {
    fun testComponentDefinedClass() {
        checkHighlighting(CssUnusedSymbolInspection())
    }

    override fun getTestDataPath(): String {
        return super.getTestDataPath() + "reference/componentCssClass"
    }
}
