package de.timo_reymann.mjml_support.util

import com.intellij.xml.util.ColorMap
import java.awt.Color
import java.util.*

object ColorUtil {
    fun parseColor(text: String?): Color? {
        text ?: return null
        return try {
            when {
                text.startsWith("rgb(") -> fromRgbString(text)
                else -> ColorMap.getColor(text)
            }
        }
        // Exception occurs after typing a comma in an argument, as then we'd
        // try to format an empty string as a number.
        catch (e: NumberFormatException) {
            null
        }
        // Exception occurs when not enough color arguments have been typed.
        // E.g. we need three arguments (r, g, b) and have typed "255, 127".
        catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    private fun fromRgbString(rgbText: String): Color {
        val rgb = rgbText
            .replace("rgb(", "")
            .replace(")", "")
            .split(",")
            .map { it.trim() }
        return try {
            rgb.map { it.toInt() }.let { Color(it[0], it[1], it[2]) }
        } catch (e: NumberFormatException) {
            rgb.map { it.toFloat() }.let { Color(it[0], it[1], it[2]) }
        }
    }

    fun toHexString(color: Color) = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
        .uppercase(Locale.getDefault())
}
