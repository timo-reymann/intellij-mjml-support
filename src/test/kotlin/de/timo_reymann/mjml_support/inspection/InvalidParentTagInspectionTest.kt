package de.timo_reymann.mjml_support.inspection

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class InvalidParentTagInspectionTest  : MjmlPluginBaseTestCase() {
    fun testValidParent() =
        checkHighlighting(InvalidParentTagInspection())

    fun testInvalidParent() =
        checkHighlighting(InvalidParentTagInspection())

    fun testWildcardParent() =
        checkHighlighting(InvalidParentTagInspection())

    fun testMjmlAttributes() =
        checkHighlighting(InvalidParentTagInspection())

    fun testNoChildren() =
        checkHighlighting(InvalidParentTagInspection())

    override fun getTestDataPath(): String = super.getTestDataPath() + "inspection/parentTag"
}
