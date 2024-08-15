package de.timo_reymann.mjml_support.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

fun loadIcon(name: String): Icon {
    return IconLoader.getIcon("/icons/editor/$name.svg", EditorIcons::class.java)
}

object EditorIcons {
    val MULTIPLATFORM_MOBILE = loadIcon("multiplatformMobile")
    val DESKTOP = loadIcon("desktop")
    val DARK_THEME = loadIcon("darkTheme")
    val LIGHT_THEME = loadIcon("lightTheme")
    val HTML = loadIcon("html")
    val REFRESH = loadIcon("refresh")
    val SYNCHRONIZE_SCROLLING = loadIcon("synchronizeScrolling")
}
