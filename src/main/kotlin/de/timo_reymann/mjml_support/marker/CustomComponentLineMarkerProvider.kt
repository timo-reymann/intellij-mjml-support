package de.timo_reymann.mjml_support.marker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import de.timo_reymann.mjml_support.api.MjmlTagInformationProvider
import de.timo_reymann.mjml_support.icons.MjmlIcons
import de.timo_reymann.mjml_support.lang.MjmlHtmlFileType
import de.timo_reymann.mjml_support.lang.MjmlHtmlLanguage
import icons.JavaScriptLanguageIcons

class CustomComponentLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is XmlToken || element.elementType != XmlTokenType.XML_NAME || element.prevSibling.elementType != XmlTokenType.XML_START_TAG_START) {
            return
        }

        for (extension in MjmlTagInformationProvider.EXTENSION_POINT.extensions) {
            val references = extension.getPsiElements(element.project, element.text)
            if (references.isNotEmpty()) {
                val builder: NavigationGutterIconBuilder<PsiElement> =
                    NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                        .setTargets(references[0].second)
                        .setTooltipText("Navigate to Simple language property")
                result.add(builder.createLineMarkerInfo(element))
                break
            }
        }

    }
}
