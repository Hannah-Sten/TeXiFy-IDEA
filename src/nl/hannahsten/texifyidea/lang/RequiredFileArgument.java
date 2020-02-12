package nl.hannahsten.texifyidea.lang;

import nl.hannahsten.texifyidea.file.FileExtensionMatcher;
import nl.hannahsten.texifyidea.file.FileNameMatcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Ignores case: everything will be converted to lower case.
 *
 * @author Hannah Schellekens
 */
public class RequiredFileArgument extends RequiredArgument implements FileNameMatcher, FileExtensionMatcher {

    private Set<String> extensions;
    private String defaultExtension;
    private Pattern pattern;
    private Boolean absolutePathSupport;

    /**
     * Create a new required file argument with a given name and a pattern that matches
     * corresponding file names.
     *
     * Type will be {@link Type#NORMAL}.
     *
     * @param name
     *         The name of the required argument.
     * @param extensions
     *         All supported extensions, of which the first extension is the default extension.
     */
    public RequiredFileArgument(String name, String... extensions) {
        this(name,true, extensions);
    }

    public RequiredFileArgument(String name, Boolean allowAbsolutePaths, String... extensions) {
        super(name, Type.FILE);
        this.absolutePathSupport = allowAbsolutePaths;

        setExtensions(extensions);
    }

    /**
     * Registers all extensions and compiles them to a regex file matcher.
     *
     * @param extensions
     *         All the file extensions (lower case) that should result in match for this required
     *         argument. The file extensions should <em>not</em> have a dot. When given no
     *         extensions, all files will match.
     */
    private void setExtensions(String... extensions) {
        this.extensions = new HashSet<>();

        StringBuilder regex = new StringBuilder(".*");

        if (extensions.length == 0) {
            setRegex(regex.toString());
            return;
        }
        else {
            this.defaultExtension = extensions[0];
        }

        regex.append("(");
        for (String extension : extensions) {
            regex.append("\\.");

            String extensionLower = extension.toLowerCase();
            regex.append(extensionLower);
            this.extensions.add(extensionLower);

            if (!extension.equals(extensions[extensions.length - 1])) {
                regex.append("|");
            }
        }
        regex.append(")$");

        setRegex(regex.toString());
    }

    private void setRegex(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Set<String> getSupportedExtensions() {
        return Collections.unmodifiableSet(extensions);
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    public Boolean isAbsolutePathSupported(){
        return absolutePathSupport;
    }

    @Override
    public boolean matchesName(String fileName) {
        return pattern.matcher(fileName.toLowerCase()).matches();
    }

    @Override
    public boolean matchesExtension(String extension) {
        return extensions.contains(extension);
    }
}
