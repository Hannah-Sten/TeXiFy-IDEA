package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.file.FileExtensionMatcher
import nl.hannahsten.texifyidea.file.FileNameMatcher
import java.util.*
import java.util.regex.Pattern

/**
 * A required file argument with a given name and a pattern that matches
 * corresponding file names.
 * Ignores case: everything will be converted to lower case.
 * Type will be [nl.hannahsten.texifyidea.lang.commands.Argument.Type.NORMAL].
 *
 * @param name
 * The name of the required argument.
 * @param commaSeparatesArguments True if arguments are separated by commas, for example \command{arg1,arg2}. If false, "arg1,arg2" will be seen as one argument.
 * @param extensions
 * All supported extensions, of which the first extension is the default extension.
 * @author Hannah Schellekens
 */
open class RequiredFileArgument(name: String?, open val isAbsolutePathSupported: Boolean = true, open val commaSeparatesArguments: Boolean, vararg extensions: String) : RequiredArgument(name!!, Type.FILE), FileNameMatcher, FileExtensionMatcher {

    lateinit var supportedExtensions: List<String>
    lateinit var defaultExtension: String
        private set
    private var pattern: Pattern? = null

    init {
        setExtensions(*extensions)
    }

    /**
     * Registers all extensions and compiles them to a regex file matcher.
     *
     * @param extensions
     * All the file extensions (lower case) that should result in match for this required
     * argument. The file extensions should *not* have a dot. When given no
     * extensions, all files will match.
     */
    private fun setExtensions(vararg extensions: String) {
        val supportedExtensions = mutableListOf<String>()
        val regex = StringBuilder(".*")
        if (extensions.isEmpty()) {
            setRegex(regex.toString())
            this.supportedExtensions = supportedExtensions
            this.defaultExtension = ""
            return
        }
        else {
            defaultExtension = extensions[0]
        }
        regex.append("(")
        for (extension in extensions) {
            regex.append("\\.")
            val extensionLower = extension.lowercase(Locale.getDefault())
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
        return pattern!!.matcher(fileName.lowercase(Locale.getDefault())).matches()
    }

    override fun matchesExtension(extension: String): Boolean {
        return supportedExtensions.contains(extension.lowercase(Locale.getDefault()))
    }
}