package de.timo_reymann.mjml_support.completion

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import org.junit.Test


class MjmlBooleanAttributeTypeCompletionContributorTest : MjmlPluginBaseTestCase() {
    fun testBlank() {
        myFixture.copyFileToProject("completion/component/ts/CustomText.ts")
        checkBasicCompletion("<custom-text bool=\"<caret>\"", "true", "false")
    }
}
