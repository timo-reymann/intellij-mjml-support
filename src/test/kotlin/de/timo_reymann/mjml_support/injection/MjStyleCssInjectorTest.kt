package de.timo_reymann.mjml_support.injection

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType

// if completion works for css properties it is properly detected
class MjStyleCssInjectorTest : MjmlPluginBaseTestCase() {

    fun testInMjStyle() {
        configureByMjmlText(
            """
            <mj-style>
                a {
                    color: <caret>;
                }
            </mj-style>
        """.trimIndent()
        )
        verifyCompletion("red", "green", "blue")
    }

    fun testInStyleAttribute() {
        configureByMjmlText( "<mj-section style='color: <caret>'></mj-section")
        verifyCompletion("red", "green", "blue")
    }
}
