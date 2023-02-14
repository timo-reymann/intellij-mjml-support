package de.timo_reymann.mjml_support.completion

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import org.junit.Test


class MjmlBooleanAttributeTypeCompletionContributorTest : MjmlPluginBaseTestCase() {
    @Test
    fun `test suggest all possible values with no prefilled value in attribute`() {
        myFixture.copyFileToProject("completion/component/ts/CustomText.ts")
        checkBasicCompletion("<custom-text bool=\"<caret>\"", "true", "false")
    }
}
