package de.timo_reymann.mjml_support.completion

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlToken
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage
import de.timo_reymann.mjml_support.model.MjmlAttributeType
import de.timo_reymann.mjml_support.model.getMjmlInfoFromAttribute
import de.timo_reymann.mjml_support.util.ColorUtil
import java.awt.Color

class MjmlColorAttributeTypeElementColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        if (element.containingFile.language != MjmlHtmlLanguage.INSTANCE || element !is XmlToken || element.elementType != XmlElementType.XML_NAME || element.parent !is XmlAttribute) {
            return null
        }
        val xmlAttribute = element.parent as XmlAttribute
        val (_, mjmlAttribute) = getMjmlInfoFromAttribute(xmlAttribute)
        if (mjmlAttribute?.type != MjmlAttributeType.COLOR) {
            return null
        }

        return ColorUtil.parseColor((element.parent as XmlAttribute).value)
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        if (element !is XmlToken) {
            return
        }

        (element.parent as XmlAttribute).setValue(ColorUtil.toHexString(color))
    }
}
