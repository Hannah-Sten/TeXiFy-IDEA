package nl.rubensten.texifyidea.util;

import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.psi.LatexCommands;

import java.util.Optional;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class TexifyUtil {

    private TexifyUtil() {
    }

    /**
     * Checks whether the command is known by TeXiFy.
     *
     * @param command
     *         The command to check.
     * @return Whether the command is known.
     */
    public static boolean isCommandKnown(LatexCommands command) {
        String commandName = Optional.ofNullable(command.getName()).map(cmd -> cmd.substring(1)).orElse("");
        return LatexNoMathCommand.get(commandName) != null || LatexMathCommand.get(commandName) != null;
    }
}