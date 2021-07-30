package de.timo_reymann.mjml_support.reference.css;

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.css.impl.util.CssReferenceProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.timo_reymann.mjml_support.index.MjmlIncludeIndex
import de.timo_reymann.mjml_support.index.util.getCssDefinedClasses
import de.timo_reymann.mjml_support.index.util.getMjmlDefinedClasses
import de.timo_reymann.mjml_support.index.util.isReachableFromReferencingElement
import de.timo_reymann.mjml_support.reference.MJML_FILE_PATTERN
import de.timo_reymann.mjml_support.util.TextRangeUtil

/**
 * Provide css class reference for stylesheets
 */
class MjmlClassReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement().inside(XmlAttributeValue::class.java).inFile(MJML_FILE_PATTERN),
            object : CssReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (element.parent !is XmlAttribute) {
                        return PsiReference.EMPTY_ARRAY
                    }
                    val attribute = (element.parent as XmlAttribute)

                    if (attribute.name != "css-class" && attribute.name != "mj-class") {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val classNames = attribute.value?.split(' ') ?: return PsiReference.EMPTY_ARRAY
                    val project = attribute.project
                    val references = mutableListOf<PsiReference>()

                    classNames.forEach { className ->
                        val textRange = TextRangeUtil.fromString(attribute.value!!, className)

                        getCssDefinedClasses(project, className)
                            .filter {
                                isReachableFromReferencingElement(
                                    project,
                                    element.containingFile,
                                    it.containingFile.virtualFile
                                )
                            }
                            .forEach {
                                references += CssDefinedClassReferenceImpl(
                                    element,
                                    textRange,
                                    it
                                )
                            }

                        getMjmlDefinedClasses(project, className)
                            .filter { isReachableFromReferencingElement(project, element.containingFile, it.second) }
                            .forEach {
                                references += MjmlDefinedClassReferenceImpl(
                                    element,
                                    it.first,
                                    it.second,
                                    textRange
                                )
                            }

                    }

                    return references.toTypedArray()
                }
            })
    }
}
