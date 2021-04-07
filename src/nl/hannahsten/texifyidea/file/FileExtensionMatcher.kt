package nl.hannahsten.texifyidea.file

/**
 * @author Hannah Schellekens
 */
@FunctionalInterface
interface FileExtensionMatcher {

    /**
     * Checks if the given extension is supported ('matched')  or not..
     *
     * @param extension
     * The extension of the file to match.
     * @return `true` if the extension matches, `false` when the extension does not
     * match.
     */
    fun matchesExtension(extension: String): Boolean
}