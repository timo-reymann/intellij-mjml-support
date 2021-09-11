package de.timo_reymann.mjml_support.editor

import de.timo_reymann.mjml_support.icons.EditorIcons
import javax.swing.Icon

enum class PreviewWidthStatus(val text: String, val description: String, val width: Int, val icon: Icon) {
    MOBILE("Mobile Preview", "Show preview for mobile devices", 400, EditorIcons.SMARTPHONE),
    DESKTOP("Desktop Preview", "Show desktop preview", 800, EditorIcons.DESKTOP);
}
