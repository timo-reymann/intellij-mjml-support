package de.timo_reymann.mjml_support.editor.render

import de.timo_reymann.mjml_support.settings.MjmlSettings
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator.AttributeWithValueMatching
import java.nio.file.Path
import java.util.regex.Pattern

private val BACKGROUND_IMAGE_URL_MATCHER = Pattern.compile("url\\('(.*)'\\)")
private val BACKGROUND_IMAGE_EVALUATOR = AttributeWithValueMatching("style", BACKGROUND_IMAGE_URL_MATCHER)

class MjmlPostProcessor(
    private val basePath: Path,
    private val mjmlSettings: MjmlSettings,
) {
    private lateinit var htmlDoc: Document

    private fun parseDocument(html: String) = Jsoup.parse(html)

    fun process(html: String): String {
        // in case we dont use local images, there is nothing else to do
        if (!mjmlSettings.resolveLocalImages) {
           return html
        }

        htmlDoc = parseDocument(html)
        processImageTags()
        processCssImageUrls()
        return htmlDoc.toString()
    }

    private fun processCssImageUrls() {
        htmlDoc.select(BACKGROUND_IMAGE_EVALUATOR)
            .parallelStream()
            .forEach(this::processCssImageUrl)
    }

    private fun processCssImageUrl(element: Element) {
        val style = element.attr("style")
        val matcher = BACKGROUND_IMAGE_URL_MATCHER.matcher(style)
        if(!matcher.find() || !isLocalImage(matcher.group(1))) {
            return
        }

        val replacedStyle = matcher.replaceFirst("url('file://$basePath/\$1')")
        element.attr("style", replacedStyle)
    }

    private fun processImageTags() {
        htmlDoc.getElementsByTag("img")
            .parallelStream()
            .forEach(this::processImageTag)
    }

    private fun processImageTag(imgTag: Element) {
        val source = imgTag.attr("src")
        if (!isLocalImage(source)) {
            return
        }
        imgTag.attr("src", "file://$basePath/$source")
    }

    private fun isLocalImage(path: String): Boolean = path.isNotEmpty() &&
            !path.startsWith("http://") &&
            !path.startsWith("https://")
}
