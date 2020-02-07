package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.file.FileExtensionMatcher
import nl.hannahsten.texifyidea.file.FileNameMatcher
import java.util.*
import java.util.regex.Pattern

/**
 * Ignores case: everything will be converted to lower case.
 *
 * @author Lukas Heiligenbrunner
 */
class RequiredFolderArgument(name: String?) : RequiredArgument(name!!, Type.FILE), FileNameMatcher, FileExtensionMatcher {
    private var extensions: MutableSet<String>? = null
    var defaultExtension: String? = null
        private set
    private var pattern: Pattern? = null
    /**
     * Registers all extensions and compiles them to a regex file matcher.
     *
     * @param extensions
     * All the file extensions (lower case) that should result in match for this required
     * argument. The file extensions should *not* have a dot. When given no
     * extensions, all files will match.
     */
    private fun setExtensions(vararg extensions: String) {
        this.extensions = HashSet()
        val regex = StringBuilder(".*")
        if (extensions.size == 0) {
            setRegex(regex.toString())
            return
        }
        else {
            defaultExtension = extensions[0]
        }
        regex.append("(")
        for (extension in extensions) {
            regex.append("\\.")
            val extensionLower = extension.toLowerCase()
            regex.append(extensionLower)
            (this.extensions as HashSet<String>).add(extensionLower)
            if (extension != extensions[extensions.size - 1]) {
                regex.append("|")
            }
        }
        regex.append(")$")
        setRegex(regex.toString())
    }

    private fun setRegex(regex: String) {
        pattern = Pattern.compile(regex)
    }

    val supportedExtensions: Set<String>
        get() = Collections.unmodifiableSet(extensions)

    override fun matchesName(fileName: String): Boolean {
        return pattern!!.matcher(fileName.toLowerCase()).matches()
    }

    override fun matchesExtension(extension: String): Boolean {
        return extensions!!.contains(extension)
    }

    /**
     * Create a new required file argument with a given name and a pattern that matches
     * corresponding file names.
     *
     * Type will be [Type.NORMAL].
     *
     * @param name
     * The name of the required argument.
     * @param extensions
     * All supported extensions, of which the first extension is the default extension.
     */
    init {
    }
}