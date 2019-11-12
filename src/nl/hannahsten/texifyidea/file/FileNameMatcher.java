package nl.hannahsten.texifyidea.file;

/**
 * @author Hannah Schellekens
 */
@FunctionalInterface
public interface FileNameMatcher {

    /**
     * Checks if the given file name matches or not.
     *
     * @param fileName
     *         The name of the file to match.
     * @return {@code true} if the fileName matches, {@code false} when the fileName does not match.
     */
    boolean matchesName(String fileName);
}
