package nl.hannahsten.texifyidea.lang.commands

import arrow.core.NonEmptySet
import nl.hannahsten.texifyidea.lang.Dependend
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.util.length
import nl.hannahsten.texifyidea.util.startsWithAny
import kotlin.reflect.KClass

/**
 * @author Hannah Schellekens, Sten Wessel
 */
interface LatexCommand : Described, Dependend {

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

    /**
     * Gets the required arguments of this command.
     */
    val requiredArguments: List<RequiredArgument>
        get() = arguments.filterIsInstance<RequiredArgument>()

    companion object {

        /**
         * Looks up the given command name in all predefined [LatexCommand]s.
         *
         * @param commandName
         *          The command name to look up. Can start with or without `\`
         * @return The found commands, or `null` when the command doesn't exist.
         */
        fun lookup(commandName: String?): NonEmptySet<LatexCommand>? {
            var result = commandName ?: return null
            if (!result.startsWith("\\")) {
                result = "\\" + result
            }
            LatexMathCommand.getWithSlash(result)?.let { return it }
            LatexRegularCommand.getWithSlash(result)?.let { return it }
            return null
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
        fun getArgumentsFromStartOfString(docs: String, counterInit: Int = 0): Array<Argument> {
            val arguments = mutableListOf<Argument>()
            var counter = counterInit
            run breaker@{
                // Only use the ones at the beginning of the string to avoid matching too much
                // I don't know what the \meta command is intended for, but it's used instead of \marg in pythontex at least
                """\s*\\(?<command>[omp]arg|meta)\{(?<arg>.+?)}\s*""".toRegex().findAll(docs, counterInit).forEach {
                    if (it.range.first == counter) {
                        if (it.groups["command"]?.value == OARG.command) {
                            arguments.add(OptionalArgument(it.groups["arg"]?.value ?: ""))
                        }
                        else {
                            arguments.add(RequiredArgument(it.groups["arg"]?.value ?: ""))
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
                return getArgumentsFromStartOfString(docs)
            }

            // Maybe the command appears somewhere in the docs with all the arguments after it
            // Check for each command in the docs,
            Regex.fromLiteral(commandWithSlash).findAll(docs).forEach { match ->
                // whether the arguments follow it.
                getArgumentsFromStartOfString(docs, match.range.last + 1).let { if (it.isNotEmpty()) return it }
            }

            return emptyArray()
        }
    }
}

data class LatexCommandImpl(
    override val commandWithSlash: String,
    override val dependency: LatexPackage,
    override val display: String? = null,
    override val description: String = "",
    override val arguments: Array<out Argument> = emptyArray(),
    override val isMathMode: Boolean = false
) : LatexCommand {
    override val command: String = commandWithSlash.substring(1)
//    override val commandWithSlash: String = "\\$command" // store it for performance reasons

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatexCommandImpl

        return identifier == other.identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }
}

internal fun String.asRequired(type: Argument.Type = Argument.Type.NORMAL) = RequiredArgument(this, type)

internal fun String.asOptional(type: Argument.Type = Argument.Type.NORMAL) = OptionalArgument(this, type)
