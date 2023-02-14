package de.timo_reymann.mjml_support.injection

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import org.junit.Test

// if completion works for css properties it is properly detected
class MjStyleCssInjectorTest : MjmlPluginBaseTestCase() {
    @Test
    fun `test It should inject CSS into mj-style tags`() {
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

    @Test
    fun `test It should inject CSS into inline style attributes`() {
        configureByMjmlText("<mj-section style='color: <caret>'></mj-section")
        verifyCompletion("red", "green", "blue")
    }
}
