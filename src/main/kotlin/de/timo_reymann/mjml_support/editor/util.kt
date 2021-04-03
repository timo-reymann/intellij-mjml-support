package de.timo_reymann.mjml_support.editor

import com.intellij.openapi.editor.colors.EditorColorsManager
import de.timo_reymann.mjml_support.util.ColorUtil

fun renderErrorHtml(body: String): String {
    val colorScheme = EditorColorsManager.getInstance().getGlobalScheme()
    return """
            <html>
                <head>
                    <style>
                        html, body {
                            background: ${ColorUtil.toHexString(colorScheme.defaultBackground)};
                            color: ${ColorUtil.toHexString(colorScheme.defaultForeground)};
                            font-family: ${colorScheme.fontPreferences.fontFamily}
                        }
                    </style>
                </head>
                    $body
                <body>
                </body>
            </html>
        """.trimIndent()
}

fun renderError(heading: String, content: String): String {
    return renderErrorHtml(
        """
            <h1>$heading</h1>
            <p>$content</p>
        """.trimIndent()
    )
}
