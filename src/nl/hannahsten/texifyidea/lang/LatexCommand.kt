package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.inMathContext
import kotlin.reflect.KClass

/**
 * @author Hannah Schellekens, Sten Wessel
 */
interface LatexCommand : Dependend {

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
         * Looks up the given command within context.
         *
         * @param command The command PSI element to look up. Takes into account whether it is placed in math mode.
         * @return The found command, or `null` when the command does not exist.
         */
        fun lookup(command: LatexCommands): Set<LatexCommand>? {
            val name = command.commandToken.text
            val commandName = name.substring(1)

            return if (command.inMathContext()) {
                LatexMathCommand[commandName]
            }
            else LatexRegularCommand[commandName]
        }
    }

    /**
     * Uniquely identifies the command, when two commands are the same, but from different packages, the identifyer
     * should be different.
     */
    val identifyer: String
        get() = commandDisplay

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