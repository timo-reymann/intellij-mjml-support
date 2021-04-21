package de.timo_reymann.mjml_support.tagprovider

import com.intellij.lang.ecmascript6.psi.ES6ClassExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSSuperClassIndex
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.CommonProcessors
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.api.MjmlTagInformationProvider

class JSES6ComponentMjmlTagInformationProvider : MjmlTagInformationProvider() {
    override fun getByTagName(project: Project, tagName: String): MjmlTagInformation? {
        return getAllPossibleComponents(project)
            .mapNotNull {
                mapElement(it)
            }
            .firstOrNull { it.tagName == tagName }
    }

    override fun getAll(project: Project): List<MjmlTagInformation> {
        val results = mutableListOf<MjmlTagInformation>()
        return getAllPossibleComponents(project)
            .mapNotNull {
                mapElement(it)
            }
            .toCollection(results)
    }

    private fun mapElement(element: PsiElement) = when (element) {
        is ES6ClassExpression -> ES6BodyComponentParser.parse(element)
        is TypeScriptClass -> MjmlCustomComponentDecoratorParser.parse(element)
        else -> null
    }

    private fun getAllPossibleComponents(project: Project): ArrayList<PsiElement> {
        val list = ArrayList<PsiElement>()
        StubIndex.getInstance()
            .processElements(
                JSSuperClassIndex.KEY,
                "BodyComponent",
                project,
                GlobalSearchScope.allScope(project),
                JSClass::class.java,
                CommonProcessors.CollectProcessor(list)
            )
        return list
    }

    override fun getPriority() = 99
}
