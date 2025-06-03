package de.timo_reymann.mjml_support.reference

import com.intellij.lang.css.CSSLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.*
import com.intellij.psi.css.resolve.StylesheetFileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.lang.model.getMjmlInfoFromAttribute

class MjmlPathAttributeTypeReferenceContributor : PsiReferenceContributor() {


    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement().inside(XmlAttributeValue::class.java).inFile(MJML_FILE_PATTERN),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val attribute = element.parentOfType<XmlAttribute>() ?: return PsiReference.EMPTY_ARRAY

                    val (_, mjmlAttribute) = getMjmlInfoFromAttribute(attribute)
                    if (mjmlAttribute?.type != MjmlAttributeType.PATH) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val filename = attribute.value ?: return PsiReference.EMPTY_ARRAY
                    val virtualFile =
                        VfsUtilCore.findRelativeFile(filename, element.containingFile.virtualFile) ?: return PsiReference.EMPTY_ARRAY

                    if (virtualFile.isDirectory) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val psiFile = PsiManager.getInstance(attribute.project).findFile(virtualFile) ?: return PsiReference.EMPTY_ARRAY

                    val fileReferenceSet = FileReferenceSet.createSet(element, false, false, false)
                    val range = TextRange(0, attribute.value!!.length + 1)

                    if(psiFile.language is CSSLanguage) {
                        return arrayOf(
                            StylesheetFileReference(
                                fileReferenceSet,
                                range,
                                0,
                                attribute.value!!
                            )
                        )
                    }

                    return arrayOf(
                        FileReference(
                            fileReferenceSet,
                            range,
                            0,
                            attribute.value!!
                        )
                    )
                }
            })
    }
}
