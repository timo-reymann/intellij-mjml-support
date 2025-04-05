// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// Ported from IntelliJ community to Kotlin
package de.timo_reymann.mjml_support.reference

import com.intellij.openapi.paths.GlobalPathReferenceProvider
import com.intellij.openapi.paths.PathReferenceManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.IssueNavigationConfiguration
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import java.util.concurrent.atomic.AtomicReference

open class ArbitraryPlaceUrlReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (Registry.`is`("ide.symbol.url.references")) {
            return PsiReference.EMPTY_ARRAY
        }
        return CachedValuesManager.getManager(element.project)
            .getParameterizedCachedValue(element, REFERENCES_KEY, PROVIDER, false, element)
    }

    override fun acceptsTarget(target: PsiElement): Boolean {
        return false
    }

    companion object {
        private val REFERENCES_KEY =
            Key.create<ParameterizedCachedValue<Array<PsiReference>, PsiElement>>("ISSUE_REFERENCES")
        private val PROVIDER: ParameterizedCachedValueProvider<Array<PsiReference>, PsiElement> =
            object : ParameterizedCachedValueProvider<Array<PsiReference>, PsiElement> {
                private val myReferenceProvider = AtomicReference<GlobalPathReferenceProvider>()

                override fun compute(element: PsiElement): CachedValueProvider.Result<Array<PsiReference>> {
                    val navigationConfiguration = IssueNavigationConfiguration.getInstance(element.project)
                        ?: return CachedValueProvider.Result.create(
                            PsiReference.EMPTY_ARRAY,
                            element
                        )

                    var refs: MutableList<PsiReference?>? = null
                    var provider = myReferenceProvider.get()
                    val commentText = StringUtil.newBombedCharSequence(element.text, 500)
                    for (link in navigationConfiguration.findIssueLinks(commentText)) {
                        if (refs == null) refs = SmartList()

                        if (provider == null) {
                            provider =
                                PathReferenceManager.getInstance().globalWebPathReferenceProvider as GlobalPathReferenceProvider
                            myReferenceProvider.lazySet(provider)
                        }
                        provider.createUrlReference(element, link.targetUrl, link.range, refs)
                    }
                    val references = refs?.toTypedArray() ?: PsiReference.EMPTY_ARRAY

                    return CachedValueProvider.Result(references, element, navigationConfiguration)
                }
            }
    }
}
