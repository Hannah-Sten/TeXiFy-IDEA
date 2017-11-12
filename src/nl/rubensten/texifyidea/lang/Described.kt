package nl.rubensten.texifyidea.lang

/**
 * Marks objects that have a description.
 *
 * @author Ruben Schellekens
 */
interface Described {

    /**
     * A short description of what the object is for.
     */
    val description: String
}