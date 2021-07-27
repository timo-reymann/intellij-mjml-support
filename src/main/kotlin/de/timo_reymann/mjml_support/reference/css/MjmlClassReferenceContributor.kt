package de.timo_reymann.mjml_support.reference.css;

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.css.CssSelectorSuffix
import com.intellij.psi.css.impl.stubs.index.CssClassIndex
import com.intellij.psi.css.impl.util.CssReferenceProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.CommonProcessors
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.timo_reymann.mjml_support.index.MjmlClassDefinitionIndex
import de.timo_reymann.mjml_support.index.MjmlIncludeIndex
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
                private fun getDeclaredCssClasses(project: Project, className: String): ArrayList<PsiElement> {
                    val occurrences = ArrayList<PsiElement>()
                    StubIndex.getInstance()
                        .processElements(
                            CssClassIndex.KEY,
                            className,
                            project,
                            GlobalSearchScope.allScope(project),
                            CssSelectorSuffix::class.java,
                            CommonProcessors.CollectProcessor(occurrences)
                        )

                    FileBasedIndex.getInstance()
                        .processValues(
                            MjmlClassDefinitionIndex.KEY,
                            className,
                            null,
                            { virtualFile, mjmlClassDefinition ->
                                val psi = PsiManager.getInstance(project).findFile(virtualFile) ?: return@processValues true
                                occurrences.add(InjectedLanguageManager.getInstance(project).findInjectedElementAt(psi ,mjmlClassDefinition.textOffset)!!)
                                true
                            },
                            GlobalSearchScope.allScope(project)
                        )

                    return occurrences
                }

                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (element.parent !is XmlAttribute) {
                        return PsiReference.EMPTY_ARRAY
                    }
                    val attribute = (element.parent as XmlAttribute)

                    if (attribute.name != "css-class") {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val classNames = attribute.value?.split(' ') ?: return PsiReference.EMPTY_ARRAY
                    val project = attribute.project
                    val references = mutableListOf<PsiReference>()

                    classNames.forEach { className ->
                        val occurrences = getDeclaredCssClasses(project, className,)

                        val textRange = TextRangeUtil.fromString(attribute.value!!, className)
                        occurrences.forEach { occurrence ->
                            val isReachableFromReferencingElement = FileBasedIndex.getInstance()
                                .getContainingFiles(
                                    MjmlIncludeIndex.KEY,
                                    MjmlIncludeIndex.createIndexKey(occurrence.containingFile.virtualFile),
                                    GlobalSearchScope.allScope(project)
                                )
                                .contains(element.containingFile.virtualFile)

                            if (isReachableFromReferencingElement) {
                                references += MjmlClassReference(element, occurrence, textRange)
                            }
                        }
                    }

                    return references.toTypedArray()
                }
            })
    }
}
