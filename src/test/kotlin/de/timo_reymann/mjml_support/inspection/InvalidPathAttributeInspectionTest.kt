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
        myFixture.copyFileToProject("invalidMjmlFile.mjml")
        checkHighlighting(InvalidPathAttributeInspection())
    }

    fun testInvalidCssFile() {
        myFixture.copyFileToProject("invalidCssFile.mjml")
        checkHighlighting(InvalidPathAttributeInspection())
    }

    fun testValidCssFile() {
        myFixture.copyFileToProject("validCssFile.mjml", "include.css")
        checkHighlighting(InvalidPathAttributeInspection())
    }

    fun testInvalidHtmlFile() {
        myFixture.copyFileToProject("invalidHtmlFile.mjml")
        checkHighlighting(InvalidPathAttributeInspection())
    }

    fun testValidHtmlFile() {
        myFixture.copyFileToProject("validHtmlFile.mjml", "include.html")
        checkHighlighting(InvalidPathAttributeInspection())
    }

    override fun getTestDataPath(): String =
        super.getTestDataPath() + "inspection/path"
}
