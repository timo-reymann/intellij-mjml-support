package de.timo_reymann.mjml_support.completion

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import org.junit.Test


class MjmlColorAttributeTypeCompletionContributorTest : MjmlPluginBaseTestCase() {
    fun testWithPrefixedCharacter() =
        checkBasicCompletion("<mj-section background-color=\"r<caret>\"", "red", "royalblue")

    fun testBlank() =
        checkBasicCompletion("<mj-section background-color=\"<caret>\"", "red", "blue", "green")

    fun testOnInvalidTag() =
        checkBasicCompletion("<some-random-tag background-color=\"<caret>\"")

    fun testNonMatching() =
        checkBasicCompletion("<mj-section background-color=\"someDefinitelyNotExistingColorGoesHere<caret>\"")
}
