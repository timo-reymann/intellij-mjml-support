package de.timo_reymann.mjml_support.completion

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlToken
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage
import de.timo_reymann.mjml_support.model.MjmlAttributeType
import de.timo_reymann.mjml_support.model.getMjmlInfoFromAttribute
import java.awt.Color

class MjmlColorAttributeTypeElementColorProvider : ElementColorProvider {
    private fun parseColor(text: String?): Color? {
        text ?: return null
        return try {
            when {
                text.startsWith("#") -> fromHtmlString(text)
                text.startsWith("rgb(") -> fromRgbString(text)
                else -> null
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
        val rgb = rgbText.split(",").map { it.trim() }
        return try {
            rgb.map { it.toInt() }.let { Color(it[0], it[1], it[2]) }
        } catch (e: NumberFormatException) {
            rgb.map { it.toFloat() }.let { Color(it[0], it[1], it[2]) }
        }
    }

    private fun fromHtmlString(htmlText: String): Color {
        return Color.decode(htmlText)
    }

    override fun getColorFrom(element: PsiElement): Color? {
        if (element.containingFile.language != MjmlHtmlLanguage.INSTANCE || element !is XmlToken || element.elementType != XmlElementType.XML_NAME || element.parent !is XmlAttribute) {
            return null
        }
        val xmlAttribute = element.parent as XmlAttribute
        val (mjmlTag, mjmlAttribute) = getMjmlInfoFromAttribute(xmlAttribute)
        if (mjmlAttribute?.type != MjmlAttributeType.COLOR) {
            return null
        }

        return parseColor((element.parent as XmlAttribute).value)
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        if (element !is XmlToken) {
            return
        }

        val hex = String.format("#%02x%02x%02x", color.red, color.blue, color.green).toUpperCase()
        (element.parent as XmlAttribute).setValue(hex)
    }
}
