package nl.hannahsten.texifyidea.run.executable

/**
 * Something that can be executed.
 */
interface Executable {

    /** Human readable name of this type, lower case. */
    val displayType: String
        get() = "executable"
}