package de.timo_reymann.mjml_support.reference

import com.intellij.testFramework.UsefulTestCase
import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class MjmlPathAttributeTypeReferenceContributorTest : MjmlPluginBaseTestCase() {
    fun testInclude() {
        myFixture.configureByFiles("directory.mjml","include.mjml")
        println(getRefsForCaret())
    }

    override fun getTestDataPath(): String = super.getTestDataPath() + "reference/path"
}
