package nl.rubensten.texifyidea.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexParameter;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class TeXiFyUtil {

    private TeXiFyUtil() {
    }

    /**
     * Looks up all the required parameters from a given {@link LatexCommands}.
     *
     * @param command
     *         The command to get the required parameters of.
     * @return A list of all required parameters.
     */
    public static List<LatexRequiredParam> getRequiredParameters(LatexCommands command) {
        return command.getParameterList().stream()
                .filter(p -> p.getRequiredParam() != null)
                .map(LatexParameter::getRequiredParam)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the given latex command marks a valid entry point for latex compilation.
     * <p>
     * A valid entry point means that a latex compilation can start from the file containing the
     * given command.
     *
     * @param command
     *         The command to check if the file marks a valid entry point.
     * @return {@code true} if the command marks a valid entry point, {@code false} if not.
     */
    public static boolean isEntryPoint(LatexCommands command) {
        // Currently: only allowing '\begin{document}'
        if (!command.getCommandToken().getText().equals("\\begin")) {
            return false;
        }

        List<LatexRequiredParam> requiredParams = getRequiredParameters(command);
        if (requiredParams.size() != 1) {
            return false;
        }

        return requiredParams.get(0).getText().equals("{document}");
    }

    /**
     * Checks if the given elements contain a valid entry point for latex compilation.
     * <p>
     * A valid entry point means that a latex compilation can start from the file containing the
     * given command.
     *
     * @param elements
     *         The elements to check for a valid entry point.
     * @return {@code true} if a valid entry point is found, {@code false} otherwise.
     */
    public static boolean containsEntryPoint(PsiElement[] elements) {
        for (PsiElement element : elements) {
            if (element instanceof LatexCommands) {
                LatexCommands commands = (LatexCommands)element;
                if (TeXiFyUtil.isEntryPoint(commands)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Sends a formatted info message to the IntelliJ logger.
     * <p>
     * All messages start with the prefix "{@code TEXIFY-IDEA - }".
     *
     * @param format
     *         How the log should be formatted, see also {@link String#format(Locale, String,
     *         Object...)}.
     * @param objects
     *         The objects to bind to the format.
     */
    public static void logf(String format, Object... objects) {
        Logger logger = Logger.getInstance(Log.class);
        logger.info("TEXIFY-IDEA - " + String.format(format, objects));
    }

    /**
     * Little class to make the log messages look awesome :3
     *
     * @author Ruben Schellekens
     */
    private class Log {

    }

}
