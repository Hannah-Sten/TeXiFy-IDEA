package nl.rubensten.texifyidea.lang

/**
 * @author Ruben Schellekens
 */
interface BibtexEntryField {

    /**
     * The name of the entry field as used as key in BibTeX tags.
     */
    val fieldName: String

    /**
     * A short description of what the entry field is for.
     */
    val description: String
}