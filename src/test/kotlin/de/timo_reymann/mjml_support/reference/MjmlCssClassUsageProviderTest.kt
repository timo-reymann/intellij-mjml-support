package de.timo_reymann.mjml_support.reference

import com.intellij.psi.css.CssClass
import com.intellij.psi.css.resolve.CssSelectorSelfReference
import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase

class MjmlCssClassUsageProviderTest : MjmlPluginBaseTestCase() {

    fun testUsage() {
        myFixture.configureByFiles("usage.mjml")
        assertEquals(
            "class",
            (getRefsForCaret()[0].resolve() as CssClass).name
        )
    }

    fun testNoUsage() {
        myFixture.configureByFiles("noUsage.mjml")
        val refs = getRefsForCaret()
        assertTrue(refs[0] is CssSelectorSelfReference)
        assertEquals(refs.size, 1)
    }

    override fun getTestDataPath(): String {
        return super.getTestDataPath() + "reference/cssClass"
    }
}
