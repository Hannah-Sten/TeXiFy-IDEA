package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.file.FileExtensionMatcher
import nl.hannahsten.texifyidea.file.FileNameMatcher
import java.util.regex.Pattern

/**
 * Ignores case: everything will be converted to lower case.
 *
 * @author Hannah Schellekens
 */
open class RequiredFileArgument(name: String?, open val isAbsolutePathSupported: Boolean = true, vararg extensions: String) : RequiredArgument(name!!, Type.FILE), FileNameMatcher, FileExtensionMatcher {
    lateinit var supportedExtensions: Set<String>
    lateinit var defaultExtension: String
        private set
    private var pattern: Pattern? = null

    init {
        setExtensions(*extensions)
    }

    /**
     * Create a new required file argument with a given name and a pattern that matches
     * corresponding file names.
     *
     * Type will be [nl.hannahsten.texifyidea.lang.Argument.Type.NORMAL].
     *
     * @param name
     * The name of the required argument.
     * @param extensions
     * All supported extensions, of which the first extension is the default extension.
     */
    constructor(name: String?, vararg extensions: String) : this(name, true, *extensions)

    /**
     * Registers all extensions and compiles them to a regex file matcher.
     *
     * @param extensions
     * All the file extensions (lower case) that should result in match for this required
     * argument. The file extensions should *not* have a dot. When given no
     * extensions, all files will match.
     */
    private fun setExtensions(vararg extensions: String) {
        val supportedExtensions = mutableSetOf<String>()
        val regex = StringBuilder(".*")
        if (extensions.isEmpty()) {
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
            supportedExtensions.add(extensionLower)
            if (extension != extensions[extensions.size - 1]) {
                regex.append("|")
            }
        }
        regex.append(")$")
        setRegex(regex.toString())

        this.supportedExtensions = supportedExtensions
    }

    private fun setRegex(regex: String) {
        pattern = Pattern.compile(regex)
    }

    override fun matchesName(fileName: String): Boolean {
        return pattern!!.matcher(fileName.toLowerCase()).matches()
    }

    override fun matchesExtension(extension: String): Boolean {
        return supportedExtensions.contains(extension)
    }
}