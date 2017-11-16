package nl.rubensten.texifyidea.lang

/**
 * @author Ruben Schellekens
 */
interface BibtexEntryField : Described {

    /**
     * The name of the entry field as used as key in BibTeX tags.
     */
    val fieldName: String
}