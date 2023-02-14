package de.timo_reymann.mjml_support.completion

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import org.junit.Test


class MjmlColorAttributeTypeCompletionContributorTest : MjmlPluginBaseTestCase() {
    @Test
    fun `test with color prefix`() =
        checkBasicCompletion("<mj-section background-color=\"r<caret>\"", "red", "royalblue")

    @Test
    fun `test with no input`() =
        checkBasicCompletion("<mj-section background-color=\"<caret>\"", "red", "blue", "green")

    @Test
    fun `test not suggest on invalid mjml tags`() =
        checkBasicCompletion("<some-random-tag background-color=\"<caret>\"")

    @Test
    fun `test not suggest any color if there is no match`() =
        checkBasicCompletion("<mj-section background-color=\"someDefinitelyNotExistingColorGoesHere<caret>\"")
}
