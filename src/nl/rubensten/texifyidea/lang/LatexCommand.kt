package nl.rubensten.texifyidea.lang

import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.inMathMode

/**
 *
 * @author Ruben Schellekens, Sten Wessel
 */
interface LatexCommand : Dependend {
    companion object {

        /**
         * Looks up the given command name in all [LatexMathCommand]s and [LatexNoMathCommand]s.
         *
         * @param commandName The command name to look up. Can start with or without `\`
         * @return The found command, or `null` when the command doesn't exist.
         */
        fun lookup(commandName: String): LatexCommand? {
            var commandName = commandName
            if (commandName.startsWith("\\")) {
                commandName = commandName.substring(1)
            }

            return LatexMathCommand.get(commandName) ?: LatexNoMathCommand.get(commandName).orElse(null)
        }

        /**
         * Looks up the given command within context.
         *
         * @param command The command PSI element to look up. Takes into account whether it is placed in math mode.
         * @return The found command, or `null` when the command does not exist.
         */
        fun lookup(command: LatexCommands): LatexCommand? {
            val commandName = command.name!!.substring(1)

            return if (command.inMathMode()) {
                LatexMathCommand.get(commandName)
            } else {
                LatexNoMathCommand.get(commandName).orElseGet(null)
            }
        }
    }

    /**
     * Get the name of the command without the first backslash.
     */
    fun getCommand(): String

    /**
     * Get the display name of the command: including backslash.
     */
    fun getCommandDisplay(): String?

    /**
     * Get all the command arguments.
     */
    fun getArguments(): Array<Argument>
}
