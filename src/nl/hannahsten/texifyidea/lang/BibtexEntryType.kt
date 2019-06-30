package nl.hannahsten.texifyidea.lang

/**
 * @author Hannah Schellekens
 */
interface BibtexEntryType : Described {

    /**
     * The token used to identify the BibTeX token _without_ `@` prefix.
     */
    val token: String

    /**
     * All required fields/keys. Empty array when no fields are required.
     */
    val required: Array<BibtexEntryField>

    /**
     * All optional fields/keys. Empty array when no fields are optional.
     */
    val optional: Array<BibtexEntryField>

    /**
     * Get all [required] and all [optional] fields. Empty collection when there are no required and no optional fields.
     */
    fun allFields(): Collection<BibtexEntryField> {
        val list: MutableList<BibtexEntryField> = ArrayList()
        list.addAll(required)
        list.addAll(optional)
        return list
    }
}