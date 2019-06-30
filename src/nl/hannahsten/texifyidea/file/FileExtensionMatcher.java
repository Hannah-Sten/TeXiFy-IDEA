package nl.hannahsten.texifyidea.file;

/**
 * @author Hannah Schellekens
 */
@FunctionalInterface
public interface FileExtensionMatcher {

    /**
     * Checks if the given extension is supported ('matched')  or not..
     *
     * @param extension
     *         The extension of the file to match.
     * @return {@code true} if the extension matches, {@code false} when the extension does not
     * match.
     */
    boolean matchesExtension(String extension);
}
