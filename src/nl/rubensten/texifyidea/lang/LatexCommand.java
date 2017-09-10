package nl.rubensten.texifyidea.lang;

import org.jetbrains.annotations.Nullable;

/**
 * @author Ruben Schellekens
 */
public interface LatexCommand extends Dependend {

    /**
     * Looks up the given command name in all {@link LatexMathCommand}s and {@link
     * LatexNoMathCommand}.
     *
     * @param commandName
     *         The command name to look up. Can start with or without {@code \}.
     * @return The found command, or {@code null} whe the command doesn't exist.
     */
    @Nullable
    static LatexCommand lookup(String commandName) {
        if (commandName.startsWith("\\")) {
            commandName = commandName.substring(1);
        }

        LatexMathCommand math = LatexMathCommand.get(commandName);
        if (math != null) {
            return math;
        }

        return LatexNoMathCommand.get(commandName).orElse(null);
    }

    /**
     * Get the name of the command without the first backslash.
     */
    String getCommand();

    /**
     * Get the display name of the command: including backslash.
     */
    String getCommandDisplay();

    /**
     * Get all the command myArguments.
     */
    Argument[] getArguments();
}
