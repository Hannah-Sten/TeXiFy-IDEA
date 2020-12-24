package nl.hannahsten.texifyidea.lang

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.index.file.LatexPackageIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.removeFileExtension
import nl.hannahsten.texifyidea.util.inMathContext
import kotlin.reflect.KClass

/**
 * @author Hannah Schellekens, Sten Wessel
 */
interface LatexCommand : Dependend {

    companion object {

        /**
         * Looks up the given command name in all [LatexMathCommand]s and [LatexRegularCommand]s.
         *
         * @param commandName
         *          The command name to look up. Can start with or without `\`
         * @return The found commands, or `null` when the command doesn't exist.
         */
        fun lookup(commandName: String?): Set<LatexCommand>? {
            var result = commandName ?: return null
            if (result.startsWith("\\")) {
                result = result.substring(1)
            }

            return LatexMathCommand[result] ?: LatexRegularCommand[result]
        }

        /**
         * Create a [LatexCommand] for the given command name.
         */
        fun lookupInIndex(cmdWithoutSlash: String, project: Project): Set<LatexCommand> {
            val cmds = mutableSetOf<LatexCommand>()
            val cmdWithSlash = "\\$cmdWithoutSlash"
            // Look up in index
            val files = FileBasedIndex.getInstance().getContainingFiles(
                LatexPackageIndex.id,
                cmdWithSlash,
                GlobalSearchScope.everythingScope(project)
            )
            for (file in files) {
                val dependency = file?.name?.removeFileExtension()
                val cmd = object : LatexCommand {
                    override val command = cmdWithoutSlash
                    override val display: String? = null
                    override val arguments = emptyArray<Argument>()
                    override val dependency =
                        if (dependency.isNullOrBlank()) LatexPackage.DEFAULT else LatexPackage(dependency)
                }
                cmds.add(cmd)
            }
            return cmds
        }

        /**
         * Looks up the given command within context.
         *
         * @param command The command PSI element to look up. Takes into account whether it is placed in math mode.
         * @return The found command, or `null` when the command does not exist.
         */
        fun lookup(command: LatexCommands): Set<LatexCommand>? {
            // todo indexed commands
            val cmdWithSlash = command.commandToken.text
            val cmdWithoutSlash = cmdWithSlash.substring(1)

            return if (command.inMathContext() && LatexMathCommand[cmdWithoutSlash] != null) {
                LatexMathCommand[cmdWithoutSlash]
            }
            else if (LatexRegularCommand[cmdWithoutSlash] != null) {
                LatexRegularCommand[cmdWithoutSlash]
            }
            else {
                lookupInIndex(cmdWithoutSlash, command.project)
            }
        }
    }

    /**
     * Get the name of the command without the first backslash.
     */
    val command: String

    val commandDisplay: String
        get() = "\\$command"

    /**
     * Get the display value of the command.
     */
    val display: String?

    /**
     * Get all the command arguments.
     */
    val arguments: Array<out Argument>

    /**
     * Concatenates all arguments to each other.
     *
     * @return e.g. `{ARG1}{ARG2}[ARG3]`
     */
    @Suppress("KDocUnresolvedReference")
    fun getArgumentsDisplay(): String {
        val sb = StringBuilder()
        for (arg in arguments) {
            sb.append(arg.toString())
        }

        return sb.toString()
    }

    /**
     * Checks whether `{}` must be automatically inserted in the auto complete.
     *
     * @return `true` to insert automatically, `false` not to insert.
     */
    fun autoInsertRequired() = arguments.filterIsInstance<RequiredArgument>().count() >= 1

    @Suppress("UNCHECKED_CAST")
    fun <T : Argument> getArgumentsOf(clazz: KClass<T>): List<T> {
        return arguments.asSequence()
            .filter { clazz.java.isAssignableFrom(it.javaClass) }
            .mapNotNull { it as? T }
            .toList()
    }

    fun <T : Argument> getArgumentsOf(clazz: Class<T>) = getArgumentsOf(clazz.kotlin)
}

internal fun String.asRequired(type: Argument.Type = Argument.Type.NORMAL) = RequiredArgument(this, type)

internal fun String.asOptional(type: Argument.Type = Argument.Type.NORMAL) = OptionalArgument(this, type)