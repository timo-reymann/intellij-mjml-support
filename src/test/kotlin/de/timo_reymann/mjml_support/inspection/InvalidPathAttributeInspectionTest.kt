package de.timo_reymann.mjml_support.inspection

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class InvalidPathAttributeInspectionTest : MjmlPluginBaseTestCase() {
    fun testNonExistingPath() =
        checkHighlighting(InvalidPathAttributeInspection())

    fun testValidPath() {
        myFixture.copyFileToProject("_include.mjml")
        checkHighlighting(InvalidPathAttributeInspection())
    }

    fun testInvalidMjmlFile() {
        myFixture.copyFileToProject("_include.mjml")
        checkHighlighting(InvalidPathAttributeInspection())
    }

    override fun getTestDataPath(): String =
        super.getTestDataPath() + "inspection/path"
}
