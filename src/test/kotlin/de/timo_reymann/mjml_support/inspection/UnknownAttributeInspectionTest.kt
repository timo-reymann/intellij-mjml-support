package de.timo_reymann.mjml_support.inspection

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class UnknownAttributeInspectionTest : MjmlPluginBaseTestCase() {
    fun testUnknownTag() =
        checkHighlighting(UnknownAttributeInspection())

    fun testAttribute() =
        checkHighlighting(UnknownAttributeInspection())

    override fun getTestDataPath(): String = super.getTestDataPath() + "inspection/unknownAttribute"
}
