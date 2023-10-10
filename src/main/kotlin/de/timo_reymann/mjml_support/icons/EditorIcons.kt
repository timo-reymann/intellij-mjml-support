package de.timo_reymann.mjml_support.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

fun loadIcon(name: String): Icon {
    return IconLoader.getIcon("/icons/editor/$name.svg", EditorIcons::class.java)
}

object EditorIcons {
    val SMARTPHONE = loadIcon("smartphone")
    val DESKTOP = loadIcon("desktop")
    val MOON = loadIcon("moon")
    val SUN = loadIcon("sun")
}
