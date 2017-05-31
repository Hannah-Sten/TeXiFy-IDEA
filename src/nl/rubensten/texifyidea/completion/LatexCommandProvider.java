package nl.rubensten.texifyidea.completion;

import com.google.common.base.Strings;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.completion.handlers.LatexCommandArgumentInsertHandler;
import nl.rubensten.texifyidea.completion.handlers.LatexNoMathInsertHandler;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexMode;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathEnvironment;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sten Wessel
 */
public class LatexCommandProvider extends CompletionProvider<CompletionParameters> {

    private LatexMode mode;

    LatexCommandProvider(LatexMode mode) {
        this.mode = mode;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context, @NotNull CompletionResultSet result) {
        switch (mode) {
            case NORMAL:
                addNormalCommands(result);
                addCustomCommands(parameters, result);
                break;
            case MATH:
                addMathCommands(result);
                addCustomCommands(parameters, result);
                break;
            case ENVIRONMENT_NAME:
                addEnvironments(result);
                break;
        }

        result.addLookupAdvertisement("Don't use \\\\ outside of tabular or math mode, it's evil.");
    }

    private void addNormalCommands(CompletionResultSet result) {
        result.addAllElements(ContainerUtil.map2List(
                LatexNoMathCommand.values(),
                cmd -> LookupElementBuilder.create(cmd, cmd.getCommand())
                        .withPresentableText(cmd.getCommandDisplay())
                        .bold()
                        .withTailText(cmd.getArgumentsDisplay(), true)
                        .withTypeText(cmd.getDisplay())
                        .withInsertHandler(new LatexNoMathInsertHandler())
                        .withIcon(TexifyIcons.DOT_COMMAND)
        ));
    }

    private void addMathCommands(CompletionResultSet result) {
        result.addAllElements(ContainerUtil.map2List(
                LatexMathCommand.values(),
                cmd -> LookupElementBuilder.create(cmd, cmd.getCommand())
                        .withPresentableText(cmd.getCommandDisplay())
                        .bold()
                        .withTailText(cmd.getArgumentsDisplay(), true)
                        .withTypeText(cmd.getDisplay())
                        .withInsertHandler(new LatexCommandArgumentInsertHandler())
                        .withIcon(TexifyIcons.DOT_COMMAND)
        ));
    }

    private void addEnvironments(CompletionResultSet result) {
        result.addAllElements(ContainerUtil.map2List(
                LatexNoMathEnvironment.values(),
                cmd -> LookupElementBuilder.create(cmd, cmd.getName())
                        .withPresentableText(cmd.getName())
                        .withIcon(TexifyIcons.DOT_ENVIRONMENT)
        ));
    }

    private void addCustomCommands(CompletionParameters parameters, CompletionResultSet result) {
        Project project = parameters.getEditor().getProject();
        PsiFile file = parameters.getOriginalFile();
        Set<VirtualFile> searchFiles = TexifyUtil.getReferencedFiles(file).stream()
                .map(PsiFile::getVirtualFile)
                .collect(Collectors.toSet());
        searchFiles.add(file.getVirtualFile());
        GlobalSearchScope scope = GlobalSearchScope.filesScope(project, searchFiles);

        Collection<LatexCommands> cmds = LatexCommandsIndex.getIndexedCommands(project, scope);

        for (LatexCommands cmd : cmds) {
            if (!isDefinition(cmd)) {
                continue;
            }

            String cmdName = getCommandName(cmd);
            String tailText = getTailText(cmd);
            String typeText = getTypeText(cmd);

            result.addElement(LookupElementBuilder.create(cmd, cmdName.substring(1))
                    .withPresentableText(cmdName)
                    .bold()
                    .withTailText(tailText, true)
                    .withTypeText(typeText, true)
                    .withInsertHandler(new LatexCommandArgumentInsertHandler())
                    .withIcon(TexifyIcons.DOT_COMMAND)
            );
        }
    }

    @NotNull
    private String getTypeText(@NotNull LatexCommands commands) {
        if ("\\newcommand".equals(commands.getCommandToken().getText())) {
            return "";
        }

        LatexCommands firstNext = TexifyUtil.getNextCommand(commands);
        if (firstNext == null) {
            return "";
        }

        LatexCommands secondNext = TexifyUtil.getNextCommand(firstNext);
        if (secondNext == null) {
            return "";
        }

        String lookup = secondNext.getCommandToken().getText();
        return lookup == null ? "" : lookup;
    }

    @NotNull
    private String getTailText(@NotNull LatexCommands commands) {
        if (!"\\newcommand".equals(commands.getCommandToken().getText())) {
            return "";
        }

        List<String> optional = commands.getOptionalParameters();

        int cmdParameterCount = 0;

        if (!optional.isEmpty()) {
            try {
                cmdParameterCount = Integer.parseInt(optional.get(0));
            }
            catch (NumberFormatException ignore) {
            }
        }

        String tailText = Strings.repeat("{param}", Math.min(4, cmdParameterCount));
        if (cmdParameterCount > 4) {
            tailText = tailText + "... (+" + (cmdParameterCount - 4) + " params)";
        }

        return tailText;
    }

    @Nullable
    private String getCommandName(@NotNull LatexCommands commands) {
        if ("\\newcommand".equals(commands.getCommandToken().getText())) {
            return getNewCommandName(commands);
        }

        return getDefinitionName(commands);
    }

    @Nullable
    private String getNewCommandName(@NotNull LatexCommands commands) {
        List<String> required = commands.getRequiredParameters();
        if (required.isEmpty()) {
            return null;
        }

        return required.get(0);
    }

    @Nullable
    private String getDefinitionName(@NotNull LatexCommands commands) {
        LatexCommands next = TexifyUtil.getNextCommand(commands);
        if (next == null) {
            return null;
        }

        return next.getCommandToken().getText();
    }

    private boolean isDefinition(@Nullable LatexCommands commands) {
        return commands != null && (
                "\\newcommand".equals(commands.getCommandToken().getText()) ||
                        "\\let".equals(commands.getCommandToken().getText()) ||
                        "\\def".equals(commands.getCommandToken().getText())
        );
    }
}
