package de.timo_reymann.mjml_support.completion

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase


class MjmlPathAttributeTypeCompletionContributorTest : MjmlPluginBaseTestCase() {
    fun testWithSingleMatch() {
        myFixture.configureByFiles("single_match/mail.mjml", "single_match/footer.mjml")
        verifyCompletion("footer.mjml")
    }

    fun testWithNoMatch() {
        myFixture.configureByFiles("no_match/mail.mjml")
        verifyCompletion()
    }

    fun testMultipleMatches() {
        myFixture.configureByFiles(
            "multiple_matches/mail.mjml",
            "multiple_matches/footer.mjml",
            "multiple_matches/header.mjml",
            "multiple_matches/stuff.txt",
            "multiple_matches/style.css"
        )
        verifyCompletion("header.mjml", "footer.mjml")
        verifyCompletion("style.css", "stuff.txt", shouldInclude = false)
    }

    override fun getTestDataPath(): String = super.getTestDataPath() + "completion/path"
}
