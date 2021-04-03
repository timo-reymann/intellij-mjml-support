package de.timo_reymann.mjml_support.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.impl.source.resolve.reference.ArbitraryPlaceUrlReferenceProvider
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.model.MjmlAttributeType
import de.timo_reymann.mjml_support.model.getMjmlInfoFromAttribute

class MjmlUrlAttributeTypeReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement().inside(XmlAttributeValue::class.java),
            object : ArbitraryPlaceUrlReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val attribute = element.parentOfType<XmlAttribute>() ?: return arrayOf()

                    val (_, mjmlAttribute) = getMjmlInfoFromAttribute(attribute)
                    if(mjmlAttribute?.type != MjmlAttributeType.URL) {
                        return arrayOf()
                    }

                    return super.getReferencesByElement(element, context)
                }
            })
    }
}
