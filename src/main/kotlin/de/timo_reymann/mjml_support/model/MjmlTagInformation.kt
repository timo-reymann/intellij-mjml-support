package de.timo_reymann.mjml_support.model

data class MjmlTagInformation(
    val tagName: String,
    val description: String,
    val notes: Array<String> = arrayOf(),
    val attributes: Array<MjmlAttributeInformation> = arrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MjmlTagInformation

        if (tagName != other.tagName) return false
        if (description != other.description) return false
        if (!notes.contentEquals(other.notes)) return false
        if (!attributes.contentEquals(other.attributes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tagName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + notes.contentHashCode()
        result = 31 * result + attributes.contentHashCode()
        return result
    }

    fun getAttributeByName(name : String): MjmlAttributeInformation? {
        return attributes.filter { it.name == name }.firstOrNull()
    }
}
