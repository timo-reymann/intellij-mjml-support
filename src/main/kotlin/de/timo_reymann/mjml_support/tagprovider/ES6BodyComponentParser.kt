package de.timo_reymann.mjml_support.tagprovider

import com.intellij.lang.ecmascript6.psi.ES6Class
import com.intellij.lang.ecmascript6.psi.ES6ClassExpression
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation
import de.timo_reymann.mjml_support.model.PARENT_ANY

object ES6BodyComponentParser {
    private const val DESCRIPTION = "ES6 Custom MJML component"

    private const val PROPERTY_ALLOWED_ATTRIBUTES = "allowedAttributes"
    private const val PROPERTY_DEFAULT_ATTRIBUTES = "defaultAttributes"

    private fun getPropertyStringValue(property: Any): String? {
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

    private fun getFieldDefinition(eS6ClassExpression: ES6ClassExpression, name: String): Array<JSProperty> =
        ((eS6ClassExpression.findFieldByName(name) as JSField).children[0] as JSObjectLiteralExpression).properties

     fun parse(expression: ES6ClassExpression): Pair<MjmlTagInformation, ES6Class>? {
        val attributeMap = mutableMapOf<String, MjmlAttributeInformation>()

        // Index all allowed properties
        val allowedProperties: Array<JSProperty> = try {
            getFieldDefinition(expression, PROPERTY_ALLOWED_ATTRIBUTES)
        } catch (e: Exception) {
            arrayOf()
        }

        // Index all possible properties
        for (allowedProperty in allowedProperties) {
            attributeMap[allowedProperty.name!!] = MjmlAttributeInformation(
                allowedProperty.name!!,
                MjmlAttributeType.fromMjmlSpec(getPropertyStringValue(allowedProperty) ?: ""),
                ""
            )
        }

        // Index default values for properties
        val defaultProperties: Array<JSProperty> = try {
            getFieldDefinition(expression, PROPERTY_DEFAULT_ATTRIBUTES)
        } catch (e: Exception) {
            arrayOf()
        }

        // Assign defaults only for existent fields
        for (defaultProperty in defaultProperties) {
            if (!attributeMap.containsKey(defaultProperty.name!!)) {
                continue
            }

            attributeMap[defaultProperty.name!!]!!.defaultValue = getPropertyStringValue(defaultProperty)
        }

        val attributes = attributeMap.entries
            .map { it.value }
            .toTypedArray()

        return Pair(
            MjmlTagInformation(
                expression.name!!.camelToKebabCase(),
                DESCRIPTION,
                attributes = attributes,
                allowedParentTags = PARENT_ANY // For now allow all tags
            ),
            expression
        )
    }
}
