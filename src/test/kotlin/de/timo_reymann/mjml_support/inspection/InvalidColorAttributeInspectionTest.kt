package de.timo_reymann.mjml_support.inspection

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class InvalidColorAttributeInspectionTest : MjmlPluginBaseTestCase() {
    fun testEmptyColor() =
        checkHighlighting(InvalidColorAttributeInspection())

    fun testInvalidColorName() =
        checkHighlighting(InvalidColorAttributeInspection())

    fun testInvalidHexColor() =
        checkHighlighting(InvalidColorAttributeInspection())

    fun testValidShortHexColor() =
        checkHighlighting(InvalidColorAttributeInspection())

    fun testValidHexColor() =
        checkHighlighting(InvalidColorAttributeInspection())

    fun testValidRgbColor() =
        checkHighlighting(InvalidColorAttributeInspection())

    override fun getTestDataPath(): String = super.getTestDataPath() + "/inspection/color"
}
