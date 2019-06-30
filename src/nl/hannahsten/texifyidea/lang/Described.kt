package nl.hannahsten.texifyidea.lang

/**
 * Marks objects that have a description.
 *
 * @author Hannah Schellekens
 */
interface Described {

    /**
     * A short description of what the object is for.
     */
    val description: String
}