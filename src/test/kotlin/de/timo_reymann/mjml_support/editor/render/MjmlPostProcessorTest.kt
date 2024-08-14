package de.timo_reymann.mjml_support.editor.render

import de.timo_reymann.mjml_support.MjmlPluginBaseTestCase
import de.timo_reymann.mjml_support.settings.MjmlSettings
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

class MjmlPostProcessorTest : MjmlPluginBaseTestCase() {
    fun createSettings(enableImages: Boolean): MjmlSettings {
        val settings = MjmlSettings()
        settings.resolveLocalImages = enableImages
        return settings
    }

    @Test
    fun `test update local images in inline style`() {
        val postProcessor = MjmlPostProcessor(File("tmp"), createSettings(true))
        val html = postProcessor.process(
            "<html>\n" +
                    " <head></head>\n" +
                    " <body>" +
                    "<table><tr>" +
                    "<td background=\"image.jpeg\" style=\"line-height: 25px; font-family: Roboto, sans-serif; background: #c6c6c6 url('image.jpeg') no-repeat center center / cover; background-position: center center; background-repeat: no-repeat; padding: 0px; vertical-align: top; font-size: 15px;\" valign=\"top\"></td>" +
                    "</tr></table>" +
                    "</body>\n" +
                    "</html>"
        )
        assertThat(html, containsString("url('file://tmp/image.jpeg')"))
    }

    @Test
    fun `test not update remote images in inline style`() {
        val postProcessor = MjmlPostProcessor(File("tmp"), createSettings(true))
        val html = postProcessor.process(
            "<html>\n" +
                    " <head></head>\n" +
                    " <body>" +
                    "<table><tr>" +
                    "<td background=\"https://cdn.com/image.jpeg\" style=\"line-height: 25px; font-family: Roboto, sans-serif; background: #c6c6c6 url('https://cdn.com/image.jpeg') no-repeat center center / cover; background-position: center center; background-repeat: no-repeat; padding: 0px; vertical-align: top; font-size: 15px;\" valign=\"top\"></td>" +
                    "</tr></table>" +
                    "</body>\n" +
                    "</html>"
        )
        assertThat(html, containsString("url('https://cdn.com/image.jpeg')"))
    }

    @Test
    fun `test update local images in image tags`() {
        val postProcessor = MjmlPostProcessor(File("tmp"), createSettings(true))
        val html = postProcessor.process(
            "<html>\n" +
                    " <head></head>\n" +
                    " <body>" +
                    "<img src=\"image.jpeg\"" +
                    "</body>\n" +
                    "</html>"
        )
        assertThat(html, containsString("src=\"file://tmp/image.jpeg\""))
    }

    @Test
    fun `test not update remote images in image tags`() {
        val postProcessor = MjmlPostProcessor(File("tmp"), createSettings(true))
        val html = postProcessor.process(
            "<html>\n" +
                    " <head></head>\n" +
                    " <body>" +
                    "<img src=\"https://cdn.com/image.jpeg\"" +
                    "</body>\n" +
                    "</html>"
        )
        assertThat(html, containsString("src=\"https://cdn.com/image.jpeg\""))
    }
}
