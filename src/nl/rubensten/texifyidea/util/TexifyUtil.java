package nl.rubensten.texifyidea.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.psi.BibtexId;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexContent;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class TexifyUtil {

    private TexifyUtil() {
    }

    /**
     * Finds all defined labels within the project matching the key.
     *
     * @param project
     *         Project scope.
     * @param key
     *         Key to match the label with.
     * @return A list of matched label commands.
     */
    public static Collection<PsiElement> findLabels(Project project, String key) {
        return LabelsKt.findLabels(project).parallelStream()
                .filter(c -> {
                    if (c instanceof LatexCommands) {
                        LatexCommands cmd = (LatexCommands)c;
                        List<String> p = ApplicationManager.getApplication().runReadAction(
                                (Computable<List<String>>)cmd::getRequiredParameters
                        );
                        return p.size() > 0 && key != null && key.equals(p.get(0));
                    }
                    else if (c instanceof BibtexId) {
                        return key != null && key.equals(((BibtexId)c).getName());
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Creates a project directory at {@code path} which will be marked as excluded.
     *
     * @param path
     *         The path to create the directory to.
     */
    public static void createExcludedDir(@NotNull String path, @NotNull Module module) {
        new File(path).mkdirs();
        // TODO: actually mark as excluded
    }

    /**
     * Retrieves the file path relative to the root path, or {@code null} if the file is not a child
     * of the root.
     *
     * @param rootPath
     *         The path of the root
     * @param filePath
     *         The path of the file
     * @return The relative path of the file to the root, or {@code null} if the file is no child of
     * the root.
     */
    @Nullable
    public static String getPathRelativeTo(@NotNull String rootPath, @NotNull String filePath) {
        if (!filePath.startsWith(rootPath)) {
            return null;
        }
        return filePath.substring(rootPath.length());
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