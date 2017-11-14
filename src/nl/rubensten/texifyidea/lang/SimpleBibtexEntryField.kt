package nl.rubensten.texifyidea.lang

/**
 * @author Ruben Schellekens
 */
data class SimpleBibtexEntryField(override val fieldName: String, override val description: String) : BibtexEntryField {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleBibtexEntryField) return false

        if (fieldName != other.fieldName) return false
        return true
    }

    override fun hashCode() = fieldName.hashCode()
}