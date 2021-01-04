package nl.hannahsten.texifyidea.lang

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.inMathContext
import nl.hannahsten.texifyidea.util.length
import nl.hannahsten.texifyidea.util.startsWithAny
import kotlin.reflect.KClass

/**
 * @author Hannah Schellekens, Sten Wessel
 */
interface LatexCommand : Described, Dependend {

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
            FileBasedIndex.getInstance().processValues(LatexExternalCommandIndex.id, cmdWithSlash, null, { file, value ->
                val dependency = LatexPackage.create(file.name)
                // Merge with already known command if possible, assuming that there was a reason to specify things (especially parameters) manually
                // Basically this means we add the indexed docs to the known command
//                val defaultcmds = lookup(cmdWithSlash)?.filter { it.dependency == dependency } ?: emptyList() todo
                val defaultcmds = emptyList<LatexCommand>()
                val cmd = if (defaultcmds.isNotEmpty()) {
                    val defaultCmd = defaultcmds.first()
                    object : LatexCommand {
                        override val command = cmdWithoutSlash
                        override val display = defaultCmd.display
                        override val arguments = defaultCmd.arguments
                        override val description = value
                        override val dependency = dependency
                    }
                }
                else {
                    object : LatexCommand {
                        override val command = cmdWithoutSlash
                        override val display: String? = null
                        override val arguments = extractArgumentsFromDocs(value, commandWithSlash)
                        override val description = value
                        override val dependency = dependency
                    }
                }
                cmds.add(cmd)
                true
            }, GlobalSearchScope.everythingScope(project))
            return cmds
        }

        /**
         * Parse arguments from docs string, assuming they appear at index [counterInit] (only initial sequence of arguments is considered).
         */
        fun getArgumentsFromStartOfString(docs: String, counterInit: Int): Array<Argument> {
            val arguments = mutableListOf<Argument>()
            var counter = counterInit
            // Only use the ones at the beginning of the string to avoid matching too much
            """\s*\\(?<command>[omp]arg)\{(?<arg>.+?)}\s*""".toRegex().findAll(docs, counterInit).forEach {
                if (it.range.first == counter) {
                    when (it.groups["command"]?.value) {
                        LatexRegularCommand.OARG.command -> arguments.add(OptionalArgument(it.groups["arg"]?.value ?: ""))
                        LatexRegularCommand.MARG.command -> arguments.add(RequiredArgument(it.groups["arg"]?.value ?: ""))
                        LatexRegularCommand.PARG.command -> arguments.add(RequiredArgument(it.groups["arg"]?.value ?: ""))
                    }
                    counter += it.range.length + 1
                }
            }

            return arguments.toTypedArray()
        }

        /**
         * todo after arguments are extracted, remove from docs?
         */
        fun extractArgumentsFromDocs(docs: String, commandWithSlash: String): Array<Argument> {
            // Maybe the arguments are given right at the beginning of the docs
            val argCommands = arrayOf(LatexRegularCommand.OARG, LatexRegularCommand.MARG, LatexRegularCommand.PARG).map { it.commandWithSlash }.toTypedArray()
            if (docs.startsWithAny(*argCommands)) {
                return getArgumentsFromStartOfString(docs, 0)
            }

            // Maybe the command appears somewhere in the docs with all the arguments after it
            // Check for each command in the docs,
            """\$commandWithSlash""".toRegex().findAll(docs).forEach { match ->
                // whether the arguments follow it.
                getArgumentsFromStartOfString(docs, match.range.last + 1).let { if (it.isNotEmpty()) return it }
            }

            return emptyArray()
        }

        /**
         * Looks up the given command within context.
         *
         * @param command The command PSI element to look up. Takes into account whether it is placed in math mode.
         * @return The found command, or `null` when the command does not exist.
         */
        fun lookup(command: LatexCommands): Set<LatexCommand>? {
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

    val commandWithSlash: String
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