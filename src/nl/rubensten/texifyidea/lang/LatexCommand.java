package nl.rubensten.texifyidea.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ruben Schellekens
 */
public interface LatexCommand {

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
     * Get all the command arguments.
     */
    Argument[] getArguments();

    /**
     * Get the package that is required for the command to work.
     *
     * @return The package object, or {@link Package#DEFAULT} when no package is needed.
     */
    @NotNull
    Package getPackage();
}
