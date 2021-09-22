package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.ui.jcef.JCEFHtmlPanel
import org.apache.commons.lang.math.RandomUtils.nextInt
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.lang.RuntimeException

class MjmlJCEFHtmlPanel : JCEFHtmlPanel(getClassUrl()) {

    companion object {
        internal const val RENDERER_ARCHIVE_NAME = "renderer.zip"

        private fun getClassUrl(): String {
            val url = try {
                val cls = MjmlJCEFHtmlPanel::class.java
                cls.getResource("${cls.simpleName}.class")?.toExternalForm() ?: error("Failed to get class URL!")
            } catch (ignored: Exception) {
                "about:blank"
            }
            return "$url@${nextInt(Integer.MAX_VALUE)}"
        }

    }

    private val handler = object : CefLoadHandlerAdapter() {

        override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
            executeJavaScript(
                // language=JavaScript
                """
                    window.scroll(0, $scrollOffset)
                    
                    function updateOffset(offset) {
                       ${query.inject("offset")}
                    }
                    document.addEventListener("scroll", () => {
                       updateOffset(window.scrollY)
                    })
                    
                    """.trimIndent()
            )
        }
    }

    internal var syncScroll: Boolean = true
    val query = createQuery()
    private var scrollOffset = 0

    init {
        query.addHandler {
            scrollOffset = it.toInt()
            null
        }
    }

    private fun createQuery(): JBCefJSQuery {
        jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 20)
        val query = JBCefJSQuery.create(this as JBCefBrowserBase)!!
        Disposer.register(ApplicationManager.getApplication(), query)
        return query
    }

    private fun executeJavaScript(code: String) {
        cefBrowser.executeJavaScript(code, null, 0)
    }

    override fun setHtml(html: String) {
        super.setHtml(html)

        jbCefClient.removeLoadHandler(handler, this.cefBrowser)

        if (this.syncScroll) {
            jbCefClient.addLoadHandler(handler, this.cefBrowser)
        }
    }
}
