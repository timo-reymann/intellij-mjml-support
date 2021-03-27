package de.timo_reymann.mjml_support.reference

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.model.getMjmlInfoFromAttribute
import de.timo_reymann.mjml_support.model.MjmlAttributeType

class MjmlPathAttributeTypeReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement().inside(XmlAttributeValue::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val attribute = element.parentOfType<XmlAttribute>() ?: return arrayOf()

                    val (mjmlTag, mjmlAttribute) = getMjmlInfoFromAttribute(attribute)
                    if(mjmlAttribute?.type != MjmlAttributeType.PATH) {
                        return arrayOf()
                    }
                    val scope = ProjectScope.getProjectScope(element.project)
                    val filename = attribute.value ?: return arrayOf()

                    VfsUtilCore.findRelativeFile(filename, element.containingFile.virtualFile) ?: return arrayOf()

                    return arrayOf(
                        FileReference(
                            FileReferenceSet.createSet(element, false, false, false),
                            TextRange(0, attribute.value!!.length), 0, attribute.value
                        )
                    )
                }
            })
    }
}
