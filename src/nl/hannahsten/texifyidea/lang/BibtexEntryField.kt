package nl.hannahsten.texifyidea.lang

/**
 * @author Hannah Schellekens
 */
interface BibtexEntryField : Described, Dependend {

    /**
     * The name of the entry field as used as key in BibTeX tags.
     */
    val fieldName: String
}