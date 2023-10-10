package de.timo_reymann.mjml_support.editor.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBColor
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.ui.jcef.JCEFHtmlPanel
import kotlinx.coroutines.selects.select
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.Color
import kotlin.random.Random.Default.nextInt

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

    enum class BackgroundMode {
        Light,
        Dark,
    }

    private val loadHandler = object : CefLoadHandlerAdapter() {
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
    private val query = createQuery()
    private var scrollOffset = 0
    private var backgroundMode: BackgroundMode = BackgroundMode.Light

    init {
        query.addHandler {
            scrollOffset = it.toInt()
            null
        }
        setBackgroundMode(backgroundMode)
    }

    private fun createQuery(): JBCefJSQuery {
        jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 20)
        val query = JBCefJSQuery.create(this as JBCefBrowserBase)
        Disposer.register(ApplicationManager.getApplication(), query)
        return query
    }

    private fun executeJavaScript(code: String) {
        cefBrowser.executeJavaScript(code, null, 0)
    }

    override fun setHtml(html: String) {
        super.setHtml(html)

        jbCefClient.removeLoadHandler(loadHandler, this.cefBrowser)
        if (this.syncScroll) {
            jbCefClient.addLoadHandler(loadHandler, this.cefBrowser)
        }
    }

    fun getBackgroundMode(): BackgroundMode = this.backgroundMode

    fun setBackgroundMode(mode: BackgroundMode) {
        this.component.background = when (mode) {
            BackgroundMode.Light -> JBColor(Color.WHITE, Color.WHITE)
            BackgroundMode.Dark -> JBColor(Color.DARK_GRAY, Color.DARK_GRAY)
        }
        this.backgroundMode = mode
    }
}
