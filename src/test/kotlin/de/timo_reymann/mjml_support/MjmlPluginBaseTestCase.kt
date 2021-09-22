package de.timo_reymann.mjml_support

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.psi.PsiReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.timo_reymann.mjml_support.inspection.InvalidColorAttributeInspection
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.jetbrains.annotations.NotNull


abstract class MjmlPluginBaseTestCase : BasePlatformTestCase() {
    protected fun getRefsForCaret(): Array<out PsiReference> {
        val element = myFixture.elementAtCaret
        return element.references
    }

    protected fun checkBasicCompletion(text: String, vararg expectedStrings: String) {
        configureByMjmlText(text)
        verifyCompletion(*expectedStrings)
    }

    protected fun checkHighlighting(inspection: InspectionProfileEntry) {
        myFixture.enableInspections(inspection)
        myFixture.testHighlighting(true, false, true, getTestName(true) + ".mjml")
    }

    protected fun verifyCompletion(vararg expectedStrings: String, shouldInclude: Boolean = true) {
        myFixture.completeBasic()
        val elements = myFixture.lookupElementStrings
        expectedStrings.forEach {
            when {
                shouldInclude -> {
                    assertThat(elements, hasItem(it))
                }
                else -> {
                    assertThat(elements, not(hasItem(it)))
                }
            }
        }

        if (shouldInclude && expectedStrings.isEmpty()) {
            assertTrue("Expected no completion results, but got ${elements!!.size}", elements.isEmpty())
        }
    }

    protected fun configureByMjmlText(text: String) {
        myFixture.configureByText(MjmlHtmlFileType.INSTANCE, text)
    }

    override fun getTestDataPath(): String = "testData/"
}
