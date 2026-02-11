package nl.hannahsten.texifyidea.lang

/**
 * @author Hannah Schellekens
 */
data class SimpleBibtexEntryField(override val fieldName: String, override val description: String, override val dependency: LatexLib = LatexLib.BASE) : BibtexEntryField {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleBibtexEntryField) return false

        return fieldName == other.fieldName
    }

    override fun hashCode() = fieldName.hashCode()
}