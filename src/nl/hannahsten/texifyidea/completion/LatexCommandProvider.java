package nl.hannahsten.texifyidea.completion;

import com.google.common.base.Strings;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandArgumentInsertHandler;
import nl.hannahsten.texifyidea.completion.handlers.LatexMathInsertHandler;
import nl.hannahsten.texifyidea.completion.handlers.LatexNoMathInsertHandler;
import nl.hannahsten.texifyidea.index.LatexCommandsIndex;
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex;
import nl.hannahsten.texifyidea.lang.*;
import nl.hannahsten.texifyidea.psi.LatexCommands;
import nl.hannahsten.texifyidea.util.*;
import nl.hannahsten.texifyidea.util.files.FileSetKt;
import nl.hannahsten.texifyidea.util.files.FilesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Hannah Schellekens, Sten Wessel
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
                addCustomCommands(parameters, result, LatexMode.MATH);
                break;
            case ENVIRONMENT_NAME:
                addEnvironments(result, parameters);
                break;
        }

        result.addLookupAdvertisement("Don't use \\\\ outside of tabular or math mode, it's evil.");
    }

    private void addNormalCommands(CompletionResultSet result) {
        result.addAllElements(ContainerUtil.map2List(
                LatexRegularCommand.values(),
                cmd -> LookupElementBuilder.create(cmd, cmd.getCommand())
                        .withPresentableText(cmd.getCommandDisplay())
                        .bold()
                        .withTailText(cmd.getArgumentsDisplay() + " " + packageName(cmd), true)
                        .withTypeText(cmd.getDisplay())
                        .withInsertHandler(new LatexNoMathInsertHandler())
                        .withIcon(TexifyIcons.DOT_COMMAND)
        ));
        result.addLookupAdvertisement(Kindness.getKindWords());
    }

    private void addMathCommands(CompletionResultSet result) {
        // Find all commands.
        List<LatexCommand> commands = new ArrayList<>();
        Collections.addAll(commands, LatexMathCommand.values());
        commands.add(LatexRegularCommand.BEGIN);

        // Create autocomplete elements.
        result.addAllElements(ContainerUtil.map2List(
                commands,
                cmd -> {
                    InsertHandler<LookupElement> handler = cmd instanceof LatexRegularCommand ?
                            new LatexNoMathInsertHandler() :
                            new LatexMathInsertHandler();


                    return LookupElementBuilder.create(cmd, cmd.getCommand())
                            .withPresentableText(cmd.getCommandDisplay())
                            .bold()
                            .withTailText(cmd.getArgumentsDisplay() + " " + packageName(cmd), true)
                            .withTypeText(cmd.getDisplay())
                            .withInsertHandler(handler)
                            .withIcon(TexifyIcons.DOT_COMMAND);
                }
        ));
    }

    private void addEnvironments(CompletionResultSet result, CompletionParameters parameters) {
        // Find all environments.
        List<Environment> environments = new ArrayList<>();
        Collections.addAll(environments, DefaultEnvironment.values());

        LatexDefinitionIndex.Companion.getItemsInFileSet(parameters.getOriginalFile()).stream()
                .filter(cmd -> Magic.Command.environmentDefinitions.contains(cmd.getName()))
                .map(cmd -> PsiCommandsKt.requiredParameter(cmd, 0))
                .filter(Objects::nonNull)
                .map(SimpleEnvironment::new)
                .forEach(environments::add);

        // Create autocomplete elements.
        result.addAllElements(ContainerUtil.map2List(
                environments,
                env -> LookupElementBuilder.create(env, env.getEnvironmentName())
                        .withPresentableText(env.getEnvironmentName())
                        .bold()
                        .withTailText(packageName(env), true)
                        .withIcon(TexifyIcons.DOT_ENVIRONMENT)
        ));
        result.addLookupAdvertisement(Kindness.getKindWords());
    }

    private String packageName(Dependend dependend) {
        String name = dependend.getDependency().getName();
        if ("".equals(name)) {
            return "";
        }

        return " (" + name + ")";
    }

    private void addCustomCommands(CompletionParameters parameters, CompletionResultSet result) {
        addCustomCommands(parameters, result, null);
    }

    private void addCustomCommands(CompletionParameters parameters, CompletionResultSet result,
                                   @Nullable LatexMode mode) {
        Project project = parameters.getEditor().getProject();
        if (project == null) {
            return;
        }

        PsiFile file = parameters.getOriginalFile();
        Set<PsiFile> files = new HashSet<>(FileSetKt.referencedFileSet(file));
        PsiFile root = FilesKt.findRootFile(file);
        PsiFile documentClass = FilesKt.documentClassFile(root);
        if (documentClass != null) {
            files.add(documentClass);
        }

        Set<VirtualFile> searchFiles = files.stream()
                .map(PsiFile::getVirtualFile)
                .collect(Collectors.toSet());
        searchFiles.add(file.getVirtualFile());
        GlobalSearchScope scope = GlobalSearchScope.filesScope(project, searchFiles);

        Collection<LatexCommands> cmds = LatexCommandsIndex.Companion.getItems(project, scope);

        for (LatexCommands cmd : cmds) {
            if (!PsiCommandsKt.isDefinition(cmd) && !PsiCommandsKt.isEnvironmentDefinition(cmd)) {
                continue;
            }

            if (mode != LatexMode.MATH && "\\DeclareMathOperator".equals(cmd.getName())) {
                continue;
            }

            String cmdName = getCommandName(cmd);
            if (cmdName == null) {
                continue;
            }

            // Skip over 'private' commands containing @ symbol in normal tex source files.
            if (!FilesKt.isClassFile(file) && !FilesKt.isStyleFile(file)) {
                if (cmdName.contains("@")) {
                    continue;
                }
            }

            String tailText = getTailText(cmd);
            String typeText = getTypeText(cmd);

            int line = 1 + StringUtil.offsetToLineNumber(cmd.getContainingFile().getText(), cmd.getTextOffset());
            typeText = typeText + " " + cmd.getContainingFile().getName() + ":" + line;

            result.addElement(LookupElementBuilder.create(cmd, cmdName.substring(1))
                    .withPresentableText(cmdName)
                    .bold()
                    .withTailText(tailText, true)
                    .withTypeText(typeText, true)
                    .withInsertHandler(new LatexCommandArgumentInsertHandler())
                    .withIcon(TexifyIcons.DOT_COMMAND)
            );
        }
        result.addLookupAdvertisement(Kindness.getKindWords());
    }

    @NotNull
    private String getTypeText(@NotNull LatexCommands commands) {
        if ("\\newcommand".equals(commands.getCommandToken().getText())) {
            return "";
        }

        LatexCommands firstNext = PsiCommandsKt.nextCommand(commands);
        if (firstNext == null) {
            return "";
        }

        LatexCommands secondNext = PsiCommandsKt.nextCommand(firstNext);
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
        switch (commands.getName()) {
            case "\\DeclareMathOperator":
            case "\\newcommand":
            case "\\newif":
                return getNewCommandName(commands);
            default:
                return getDefinitionName(commands);
        }
    }

    @Nullable
    private String getNewCommandName(@NotNull LatexCommands commands) {
        LatexCommands cmd = PsiCommandsKt.forcedFirstRequiredParameterAsCommand(commands);
        return cmd == null ? null : cmd.getName();
    }

    @Nullable
    private String getDefinitionName(@NotNull LatexCommands commands) {
        LatexCommands next = PsiCommandsKt.definitionCommand(commands);
        if (next == null) {
            return null;
        }

        return next.getCommandToken().getText();
    }
}
