package de.timo_reymann.mjml_support.inspection

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class InvalidUrlAttributeInspectionTest : MjmlPluginBaseTestCase() {
    fun testHttpsUrl() =
        checkHighlighting(InvalidUrlAttributeInspection())

    fun testHttpUrl() =
        checkHighlighting(InvalidUrlAttributeInspection())

    fun testTel() =
        checkHighlighting(InvalidUrlAttributeInspection())

    fun testMail() =
        checkHighlighting(InvalidUrlAttributeInspection())

    override fun getTestDataPath(): String = super.getTestDataPath() +"inspection/url"
}
