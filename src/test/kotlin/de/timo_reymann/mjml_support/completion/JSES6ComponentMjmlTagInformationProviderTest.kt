package de.timo_reymann.mjml_support.completion

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.model.PARENT_ANY
import de.timo_reymann.mjml_support.tagprovider.custom.JSES6ComponentMjmlTagInformationProvider
import org.junit.Assert
import org.junit.Test

class JSES6ComponentMjmlTagInformationProviderTest : MjmlPluginBaseTestCase() {
    @Test
    fun `test find plain js components`() {
        myFixture.copyFileToProject("es6/TestComponent.js")
        val provider = JSES6ComponentMjmlTagInformationProvider()
        val tagInfo = provider.getByTagName(myFixture.project, "test-component")
        Assert.assertNotNull(tagInfo)

        Assert.assertEquals("test-component", tagInfo!!.tagName)
        Assert.assertEquals(PARENT_ANY, tagInfo.allowedParentTags)
        Assert.assertEquals(5, tagInfo.attributes.size)

        verifyAttribute(tagInfo, "color", MjmlAttributeType.COLOR, "black")
        verifyAttribute(tagInfo, "stars-color", MjmlAttributeType.COLOR, "yellow")
        verifyAttribute(tagInfo, "font-size", MjmlAttributeType.PIXEL, "12px")
        verifyAttribute(tagInfo, "align", MjmlAttributeType.STRING, "center")
        verifyAttribute(tagInfo, "text", MjmlAttributeType.STRING, null)
    }

    @Test
    fun `test resolve typescript decorator based components`() {
        myFixture.copyFileToProject("ts/CustomText.ts")
        val provider = JSES6ComponentMjmlTagInformationProvider()
        val tagInfo = provider.getByTagName(myFixture.project, "custom-text")
        Assert.assertNotNull(tagInfo)

        Assert.assertEquals("custom-text", tagInfo!!.tagName)
        Assert.assertEquals(1, tagInfo.allowedParentTags.size)
        Assert.assertEquals("mj-column", tagInfo.allowedParentTags[0])

        verifyAttribute(tagInfo, "text", MjmlAttributeType.STRING, "Hello World")
        verifyAttribute(tagInfo, "color", MjmlAttributeType.COLOR, null)
        verifyAttribute(tagInfo, "empty", MjmlAttributeType.STRING, null)
        verifyAttribute(tagInfo, "bool", MjmlAttributeType.BOOLEAN, "false")
    }

    private fun verifyAttribute(mjmlTag: MjmlTagInformation, name: String, type: MjmlAttributeType, default: String?) {
        val attributeByName = mjmlTag.getAttributeByName(name)
        Assert.assertNotNull(attributeByName)
        Assert.assertEquals(type, attributeByName!!.type)
        Assert.assertEquals(default, attributeByName.defaultValue)
    }

    override fun getTestDataPath(): String = super.getTestDataPath() + "completion/component"
}
