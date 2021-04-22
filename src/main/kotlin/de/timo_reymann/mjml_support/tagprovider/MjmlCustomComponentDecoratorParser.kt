package de.timo_reymann.mjml_support.tagprovider

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import de.timo_reymann.mjml_support.api.MjmlAttributeInformation
import de.timo_reymann.mjml_support.api.MjmlAttributeType
import de.timo_reymann.mjml_support.api.MjmlTagInformation

object MjmlCustomComponentDecoratorParser {
    private const val ANNOTATION_NAME = "MJMLCustomComponent"
    private const val DESCRIPTION = "Component annotated with @$ANNOTATION_NAME"

    private const val PROPERTY_DEFAULT_VALUE = "default"
    private const val PROPERTY_TYPE = "type"

    fun parse(tsClass: TypeScriptClass): Pair<MjmlTagInformation, TypeScriptClass>? {
        val attributeList = tsClass.attributeList ?: return null
        val decorators = attributeList.decorators
        for (decorator in decorators) {
            // Skip without decorators
            if (decorator.decoratorName != ANNOTATION_NAME) {
                continue
            }

            // Parse object from decorator
            val argumentList = (decorator.children[0] as JSCallExpression).argumentList ?: return null
            var definition: JSObjectLiteralExpression? = null
            for (child in argumentList.children) {
                if (child is JSObjectLiteralExpression) {
                    definition = child
                    break
                }
            }

            // If config is not present, skip
            if (definition == null) {
                return null
            }

            val attributes = parseAttributes(definition)
            val allowedParents = parseAllowedParentTags(definition)

            return Pair(
                MjmlTagInformation(
                    tsClass.name.toString().camelToKebabCase(),
                    DESCRIPTION,
                    attributes = attributes.toTypedArray(),
                    allowedParentTags = allowedParents
                ),
                tsClass
            )
        }
        return null
    }

    private fun parseAllowedParentTags(definition: JSObjectLiteralExpression): MutableList<String> {
        val decoratorParentTags = definition.findProperty("allowedParentTags")
        val allowedParents = mutableListOf<String>()
        if (decoratorParentTags != null && decoratorParentTags.value is JSArrayLiteralExpression) {
            for (decoratorParentTag in decoratorParentTags.value!!.children) {
                // Only process valid parent tags
                if (decoratorParentTag !is JSLiteralExpression || decoratorParentTag.value !is String) {
                    continue
                }

                allowedParents.add(decoratorParentTag.value as String)
            }
        }
        return allowedParents
    }

    private fun getPropertyValue(jsProperty: JSProperty?): String? =
        (jsProperty?.value as JSLiteralExpression?)?.value?.toString()

    private fun parseAttributes(definition: JSObjectLiteralExpression): MutableList<MjmlAttributeInformation> {
        val decoratorAttributes = definition.findProperty("attributes")
        val attributes = mutableListOf<MjmlAttributeInformation>()

        // Only process valid objects
        if (decoratorAttributes == null || decoratorAttributes.value !is JSObjectLiteralExpression) {
            return attributes
        }

        val attributesDefinition = decoratorAttributes.value as JSObjectLiteralExpression? ?: return attributes
        for (decoratorAttributeSpec in attributesDefinition.properties) {
            // Only process valid properties
            if (decoratorAttributeSpec !is JSProperty || decoratorAttributeSpec.value !is JSObjectLiteralExpression) {
                continue
            }

            val attributeSpec = (decoratorAttributeSpec.value as JSObjectLiteralExpression)
            val type = MjmlAttributeType.fromMjmlSpec(getPropertyValue(attributeSpec.findProperty(PROPERTY_TYPE)) ?: "")
            val default = getPropertyValue(attributeSpec.findProperty(PROPERTY_DEFAULT_VALUE))
            attributes.add(MjmlAttributeInformation(decoratorAttributeSpec.name!!, type, "", default))
        }

        return attributes
    }
}
