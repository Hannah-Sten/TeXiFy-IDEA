package nl.rubensten.texifyidea.lang

import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.inMathContext
import kotlin.reflect.KClass

/**
 * @author Ruben Schellekens, Sten Wessel
 */
interface LatexCommand : Dependend {

    companion object {

        /**
         * Looks up the given command name in all [LatexMathCommand]s and [LatexNoMathCommand]s.
         *
         * @param commandName
         *          The command name to look up. Can start with or without `\`
         * @return The found command, or `null` when the command doesn't exist.
         */
        fun lookup(commandName: String?): LatexCommand? {
            var result = commandName ?: return null
            if (result.startsWith("\\")) {
                result = result.substring(1)
            }

            return LatexMathCommand[result] ?: LatexNoMathCommand[result]
        }

        /**
         * Looks up the given command within context.
         *
         * @param command The command PSI element to look up. Takes into account whether it is placed in math mode.
         * @return The found command, or `null` when the command does not exist.
         */
        fun lookup(command: LatexCommands): LatexCommand? {
            val name = command.commandToken.text
            val commandName = name.substring(1)

            return if (command.inMathContext()) {
                LatexMathCommand[commandName]
            }
            else LatexNoMathCommand[commandName]
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
    fun autoInsertRequired() = arguments.filter { arg -> arg is RequiredArgument }.count() >= 1

    @Suppress("UNCHECKED_CAST")
    fun <T : Argument> getArgumentsOf(clazz: KClass<T>): List<T> {
        return arguments
                .filter { clazz.java.isAssignableFrom(it.javaClass) }
                .mapNotNull { it as? T }
    }

    fun <T : Argument> getArgumentsOf(clazz: Class<T>) = getArgumentsOf(clazz.kotlin)
}

internal fun String.asRequired(type: Argument.Type = Argument.Type.NORMAL) = RequiredArgument(this, type)

internal fun String.asOptional(type: Argument.Type = Argument.Type.NORMAL) = OptionalArgument(this, type)