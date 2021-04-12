package de.timo_reymann.mjml_support.model

import com.intellij.lang.ecmascript6.psi.ES6ClassExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSSubclassIndex
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.CommonProcessors
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.api.MjmlTagInformationProvider

val kebabCase = "(?<=[a-zA-Z])[A-Z]".toRegex()

fun String.camelToKebabCase(): String {
    return kebabCase.replace(this) {
        "-${it.value}"
    }.toLowerCase()
}

class JSComponentMjmlTagInformationProvider : MjmlTagInformationProvider() {
    override fun getByTagName(project: Project, tagName: String): MjmlTagInformation? {
        return getAllPossibleComponents(project)
            .map { classExpressionToTagInformation(it) }
            .firstOrNull { it.tagName == tagName }
    }

    override fun getAll(project: Project): List<MjmlTagInformation> {
        val results = mutableListOf<MjmlTagInformation>()
        return getAllPossibleComponents(project)
            .map { classExpressionToTagInformation(it) }
            .toCollection(results)
    }

    private fun getAllPossibleComponents(project: Project): List<ES6ClassExpression> {
        // TODO Further validation, error handling
        val list = ArrayList<PsiElement>()
        StubIndex.getInstance()
            .processElements(
                JSSubclassIndex.KEY,
                "BodyComponent",
                project,
                GlobalSearchScope.allScope(project),
                JSElement::class.java,
                CommonProcessors.CollectProcessor(list)
            )
        return list.filterIsInstance<ES6ClassExpression>()
    }

    private fun classExpressionToTagInformation(expression: ES6ClassExpression): MjmlTagInformation {
        val properties =
            ((expression.findFieldByName("allowedAttributes") as JSField).children[0] as JSObjectLiteralExpression).properties
        val attributes = arrayListOf<MjmlAttributeInformation>()

        // TODO Map default values

        for (property in properties) {
            attributes.add(MjmlAttributeInformation(property.name!!, MjmlAttributeType.STRING, ""))
        }

        return MjmlTagInformation(
            expression.name!!.camelToKebabCase(),
            "",
            attributes = attributes.toTypedArray(),
            allowedParentTags = PARENT_ANY
        )
    }
}
