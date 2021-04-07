package nl.hannahsten.texifyidea.file

/**
 * @author Hannah Schellekens
 */
@FunctionalInterface
interface FileNameMatcher {

    /**
     * Checks if the given file name matches or not.
     *
     * @param fileName
     * The name of the file to match.
     * @return `true` if the fileName matches, `false` when the fileName does not match.
     */
    fun matchesName(fileName: String): Boolean
}