package de.timo_reymann.mjml_support.completion

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import org.junit.Ignore


class MjmlPathAttributeTypeCompletionContributorTest : MjmlPluginBaseTestCase() {
    fun testWithSingleMatch() {
        myFixture.configureByFiles("single_match/mail.mjml", "single_match/footer.mjml")
        verifyCompletion("footer.mjml", shouldInclude = true)
    }

    @Ignore // ignored for now
    fun testWithNoMatch() {
        myFixture.configureByFiles("no_match/mail.mjml")
        verifyCompletion()
    }

    fun testWithCssMatch() {
        myFixture.configureByFiles("css_match/mail.mjml", "css_match/footer.css", "css_match/file.mjml")
        verifyCompletion("footer.css", shouldInclude = true)
        verifyCompletion("file.mjml", shouldInclude = false)
    }

    fun testMultipleMatches() {
        myFixture.configureByFiles(
            "multiple_matches/mail.mjml",
            "multiple_matches/footer.mjml",
            "multiple_matches/header.mjml",
            "multiple_matches/stuff.txt",
            "multiple_matches/style.css"
        )
        verifyCompletion("header.mjml", "footer.mjml", shouldInclude = true)
        verifyCompletion("style.css", "stuff.txt", shouldInclude = false)
    }

    override fun getTestDataPath(): String = super.getTestDataPath() + "completion/path"
}
