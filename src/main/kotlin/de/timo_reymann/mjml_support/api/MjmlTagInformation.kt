package de.timo_reymann.mjml_support.api

import de.timo_reymann.mjml_support.model.PARENT_ANY

/**
 * Information about mjml tag
 */
data class MjmlTagInformation(
    /**
     * Name of the tag
     */
    val tagName: String,

    /**
     * Description for tool tips
     */
    val description: String,

    /**
     * Allowed parent tags, following magic values are supported:
     *
     * - `*` star allows any parent tag
     * - empty list allows no parent excerpt the top level document
     */
    val allowedParentTags: List<String>,

    /**
     * Notes about the element, anything special or warnings go here
     */
    val notes: Array<String> = arrayOf(),

    /**
     * List with valid attributes on this element
     */
    val attributes: Array<MjmlAttributeInformation> = arrayOf(),

    /**
     * List with classes defined by the component, useful for e. g. class usage detection
     */
    val definedCssClasses: Array<String> = arrayOf(),

    /**
     * Is the tag allowed to have children tags or not
     */
    val canHaveChildren: Boolean = true
) {
    fun getAttributeByName(name: String): MjmlAttributeInformation? = attributes.firstOrNull { it.name == name }

    fun isValidParent(tag: MjmlTagInformation): Boolean {
        if (!tag.canHaveChildren) {
            return false;
        }

        return allowedParentTags == PARENT_ANY
                || allowedParentTags.contains(tag.tagName)
                || tag.tagName == "mj-attributes"
    }

    fun definesClass(cssClass: String): Boolean {
        return definedCssClasses.contains(cssClass)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MjmlTagInformation

        if (tagName != other.tagName) return false
        if (description != other.description) return false
        if (!notes.contentEquals(other.notes)) return false
        if (!attributes.contentEquals(other.attributes)) return false
        if (allowedParentTags != other.allowedParentTags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tagName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + notes.contentHashCode()
        result = 31 * result + attributes.contentHashCode()
        result = 31 * result + allowedParentTags.hashCode()
        return result
    }

    override fun toString(): String {
        return "MjmlTagInformation(tagName='$tagName', description='$description', allowedParentTags=$allowedParentTags, notes=${notes.contentToString()}, attributes=${attributes.contentToString()}, definedCssClasses=${definedCssClasses.contentToString()}, canHaveChildren=$canHaveChildren)"
    }
}
