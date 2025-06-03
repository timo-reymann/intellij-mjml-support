package de.timo_reymann.mjml_support.editor.rendering

import com.intellij.openapi.editor.colors.EditorColorsManager
import de.timo_reymann.mjml_support.util.ColorUtil

fun renderErrorHtml(body: String): String {
    val colorScheme = EditorColorsManager.getInstance().globalScheme
    return """
            <html>
                <head>
                    <style>
                        html, body {
                            background: ${ColorUtil.toHexString(colorScheme.defaultBackground)};
                            color: ${ColorUtil.toHexString(colorScheme.defaultForeground)};
                            font-family: ${colorScheme.fontPreferences.fontFamily}
                        }
                        
                        svg {
                            fill: ${ColorUtil.toHexString(colorScheme.defaultForeground)};
                            width: 64px;
                        }
                        
                        header {
                            display: flex;
                            justify-content: center;
                            gap: 10px;
                        }
                        
                        body {
                            text-align: center;
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
            <header>
                <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" xmlns:xlink="http://www.w3.org/1999/xlink" enable-background="new 0 0 512 512">
                  <path d="m507.641,431.876l-224-384.002c-5.734-9.828-16.258-15.875-27.641-15.875-11.383,0-21.906,6.047-27.641,15.875l-224,384.002c-5.773,9.898-5.813,22.125-0.109,32.063 5.711,9.938 16.289,16.063 27.75,16.063h448.001c11.461,0 22.039-6.125 27.75-16.063 5.703-9.938 5.664-22.165-0.11-32.063zm-251.641-15.878c-17.656,0-32-14.328-32-32 0-17.672 14.344-32 32-32 17.688,0 32,14.328 32,32 0,17.671-14.312,32-32,32zm32-127.998c0,17.672-14.328,32-32,32s-32-14.328-32-32v-96c0-17.672 14.328-32 32-32s32,14.328 32,32v96z"/>
                </svg>
    
                <h1>$heading</h1>
            </header>
            <p>$content</p>
        """.trimIndent()
    )
}
