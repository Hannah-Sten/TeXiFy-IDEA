package nl.rubensten.texifyidea.util;

import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexContent;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class TexifyUtil {

    private TexifyUtil() {
    }

    /**
     * Returns the forced first required parameter of a command as a command.
     * <p>
     * This allows both example constructs {@code \\usepackage{\\foo}} and {@code
     * \\usepackage\\foo}, which are equivalent. Note that when the command does not take parameters
     * this method might return untrue results.
     *
     * @param command
     *         The command to get the parameter for.
     * @return The forced first required parameter of the command.
     */
    public static LatexCommands getForcedFirstRequiredParameterAsCommand(LatexCommands command) {
        List<LatexRequiredParam> params = PsiCommandsKt.requiredParameters(command);
        if (params.size() > 0) {
            LatexRequiredParam param = params.get(0);
            Collection<LatexCommands> found = PsiTreeUtil.findChildrenOfType(param, LatexCommands.class);
            if (found.size() == 1) {
                return (LatexCommands)(found.toArray()[0]);
            }
            else {
                return null;
            }
        }

        LatexContent sibling = PsiTreeUtil.getNextSiblingOfType(PsiTreeUtil.getParentOfType(command, LatexContent.class), LatexContent.class);
        return PsiTreeUtil.findChildOfType(sibling, LatexCommands.class);
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