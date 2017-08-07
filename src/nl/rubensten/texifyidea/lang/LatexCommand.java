package nl.rubensten.texifyidea.lang;

import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens
 */
public interface LatexCommand {

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
