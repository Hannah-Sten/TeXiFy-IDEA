package nl.hannahsten.texifyidea.lang.commands

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex
import nl.hannahsten.texifyidea.lang.Dependend
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
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
         * Looks up the given command name in all predefined [LatexCommand]s.
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
         * Create a [LatexCommand] for the given command name, or merge with existing one.
         */
        fun lookupInIndex(cmdWithoutSlash: String, project: Project): Set<LatexCommand> {
            // Don't try to access index when in dumb mode
            if (DumbService.isDumb(project)) return emptySet()
            val cmdWithSlash = "\\$cmdWithoutSlash"

            // Make sure to look up the hardcoded commands, to have something in case nothing is found in the index
            val cmds = lookup(cmdWithSlash)?.toMutableSet() ?: mutableSetOf()

            // Look up in index
            FileBasedIndex.getInstance().processValues(
                LatexExternalCommandIndex.id, cmdWithSlash, null, { file, value ->
                    val dependency = LatexPackage.create(file)
                    // Merge with already known command if possible, assuming that there was a reason to specify things (especially parameters) manually
                    // Basically this means we add the indexed docs to the known command
                    val defaultcmds = lookup(cmdWithSlash)?.filter { it.dependency == dependency } ?: emptyList()
                    val cmd = if (defaultcmds.isNotEmpty()) {
                        val defaultCommand = defaultcmds.first()
                        object : LatexCommand {
                            override val command = cmdWithoutSlash
                            override val display = defaultCommand.display
                            override val arguments = defaultCommand.arguments
                            override val description = format(value)
                            override val dependency = dependency
                            override val isMathMode = defaultCommand.isMathMode
                        }
                    }
                    else {
                        object : LatexCommand {
                            override val command = cmdWithoutSlash
                            override val display: String? = null
                            override val arguments = extractArgumentsFromDocs(value, commandWithSlash)
                            override val description = format(value)
                            override val dependency = dependency
                            override val isMathMode = false
                        }
                    }
                    cmds.add(cmd)
                    true
                },
                GlobalSearchScope.everythingScope(project)
            )

            // Now we might have duplicates, some of which might differ only in description.
            // Of those, we just want to take any command which doesn't have an empty description if it exists
            // Since an interface cannot override equals, and it has to be an interface because enums implement it, filter duplicates first
            return cmds.distinctBy { listOf(it.command, it.dependency, it.isMathMode, it.description).plus(it.arguments) }
                // Do not group by arguments, because if there is a difference in arguments, probably the command with better (non-empty) docs is also the one with better argument info
                .groupBy { listOf(it.command, it.dependency, it.isMathMode) }
                // Assume empty descriptions appear first when sorted
                .mapValues { it.value.maxByOrNull { cmd -> cmd.description }!! }
                .values.toSet()
        }

        /**
         * Do the last bit of formatting, to remove things that [nl.hannahsten.texifyidea.index.file.LatexDocsRegexer] needed to keep in because we needed the information here.
         */
        fun format(docs: String): String {
            return """^(?:\s*\\[mop]arg\{[^}]+}\s*)*(?:\\\\)?\s*""".toRegex().replace(docs, "")
        }

        /**
         * Parse arguments from docs string, assuming they appear at index [counterInit] (only initial sequence of arguments is considered).
         */
        fun getArgumentsFromStartOfString(docs: String, counterInit: Int): Array<Argument> {
            val arguments = mutableListOf<Argument>()
            var counter = counterInit
            run breaker@{
                // Only use the ones at the beginning of the string to avoid matching too much
                """\s*\\(?<command>[omp]arg)\{(?<arg>.+?)}\s*""".toRegex().findAll(docs, counterInit).forEach {
                    if (it.range.first == counter) {
                        when (it.groups["command"]?.value) {
                            OARG.command -> arguments.add(OptionalArgument(it.groups["arg"]?.value ?: ""))
                            MARG.command -> arguments.add(RequiredArgument(it.groups["arg"]?.value ?: ""))
                            PARG.command -> arguments.add(RequiredArgument(it.groups["arg"]?.value ?: ""))
                        }
                        counter += it.range.length + 1
                    }
                    else {
                        return@breaker
                    }
                }
            }

            // Special convention in LaTeX base
            if (arguments.isEmpty()) {
                run breaker@{
                    counter = counterInit
                    """\s*(?:\{\\meta\{(?<marg>.+?)}}|\[\\meta\{(?<oarg>.+?)}])""".toRegex().findAll(docs, counterInit).forEach { match ->
                        if (match.range.first == counter) {
                            match.groups["marg"]?.value?.let {
                                arguments.add(RequiredArgument(it))
                            }
                            match.groups["oarg"]?.value?.let {
                                arguments.add(OptionalArgument(it))
                            }
                            counter += match.range.length + 1
                        }
                        else {
                            return@breaker
                        }
                    }
                }
            }

            return arguments.toTypedArray()
        }

        /**
         * Given the [docs] of the [commandWithSlash], check if the parameters of the [commandWithSlash] are documented in [docs] and if so, return them.
         */
        fun extractArgumentsFromDocs(docs: String, commandWithSlash: String): Array<Argument> {
            // Maybe the arguments are given right at the beginning of the docs
            val argCommands = arrayOf(OARG, MARG, PARG).map { it.commandWithSlash }.toTypedArray()
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
            else {
                // Attempt to avoid an error about slow operations on EDT
                runBlocking {
                    lookupInIndex(cmdWithoutSlash, command.project)
                }
            }
        }
    }

    /**
     * Uniquely identifies the command, when two commands are the same, but from different packages, the identifier
     * should be different.
     */
    val identifier: String
        get() = commandWithSlash

    override val description: String
        get() = identifier

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
     * Whether this command must be used in math mode (`true`).
     */
    val isMathMode: Boolean

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
    fun autoInsertRequired() = arguments.filterIsInstance<RequiredArgument>().isNotEmpty()

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