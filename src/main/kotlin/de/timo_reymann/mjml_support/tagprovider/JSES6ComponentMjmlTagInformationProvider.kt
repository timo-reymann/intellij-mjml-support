package de.timo_reymann.mjml_support.tagprovider

import com.intellij.lang.ecmascript6.psi.ES6ClassExpression
import com.intellij.lang.javascript.psi.*
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
import de.timo_reymann.mjml_support.model.PARENT_ANY
import org.jetbrains.annotations.NotNull

val kebabCase = "(?<=[a-zA-Z])[A-Z]".toRegex()

fun String.camelToKebabCase(): String {
    return kebabCase
        .replace(this) { "-${it.value}" }
        .toLowerCase()
}

class JSES6ComponentMjmlTagInformationProvider : MjmlTagInformationProvider() {
    override fun getByTagName(project: Project, tagName: String): MjmlTagInformation? {
        return getAllPossibleComponents(project)
            .mapNotNull { classExpressionToTagInformation(it) }
            .firstOrNull { it.tagName == tagName }
    }

    override fun getAll(project: Project): List<MjmlTagInformation> {
        val results = mutableListOf<MjmlTagInformation>()
        return getAllPossibleComponents(project)
            .mapNotNull { classExpressionToTagInformation(it) }
            .toCollection(results)
    }

    private fun getAllPossibleComponents(project: Project): List<ES6ClassExpression> {
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

    private fun getPropertyValue(property: Any): String? {
        if (property !is JSProperty) {
            return null
        }

        if (property.value !is JSLiteralExpression) {
            return null
        }

        val propertyValue = (property.value as JSLiteralExpression)
        if (propertyValue.value == null) {
            return null
        }

        return propertyValue.value.toString()
    }

    private fun getFieldDefinition(eS6ClassExpression: ES6ClassExpression, name: String): @NotNull Array<JSProperty> {
        return ((eS6ClassExpression.findFieldByName(name) as JSField).children[0] as JSObjectLiteralExpression).properties
    }

    private fun classExpressionToTagInformation(expression: ES6ClassExpression): MjmlTagInformation? {
        val attributeMap = mutableMapOf<String, MjmlAttributeInformation>()

        // Index all allowed properties
        val allowedProperties: Array<JSProperty> = try {
            getFieldDefinition(expression, "allowedAttributes")
        } catch (e: Exception) {
            arrayOf()
        }

        // Index all possible properties
        for (allowedProperty in allowedProperties) {
            attributeMap[allowedProperty.name!!] = MjmlAttributeInformation(
                allowedProperty.name!!,
                MjmlAttributeType.fromMjmlSpec(getPropertyValue(allowedProperty) ?: ""),
                ""
            )
        }

        // Index default values for properties
        val defaultProperties: Array<JSProperty> = try {
            getFieldDefinition(expression, "defaultAttributes")
        } catch (e: Exception) {
            arrayOf()
        }

        // Assign defaults only for existent fields
        for (defaultProperty in defaultProperties) {
            if (!attributeMap.containsKey(defaultProperty.name!!)) {
                continue
            }

            attributeMap[defaultProperty.name!!]!!.defaultValue = getPropertyValue(defaultProperty)
        }

        val attributes = attributeMap.entries
            .map { it.value }
            .toTypedArray()

        return MjmlTagInformation(
            expression.name!!.camelToKebabCase(),
            "",
            attributes = attributes,
            allowedParentTags = PARENT_ANY // For now allow all tags
        )
    }

    override fun getPriority() = 99
}
