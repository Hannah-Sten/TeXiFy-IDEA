package nl.rubensten.texifyidea.completion;

import com.google.common.base.Strings;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

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
                addCustomCommands(parameters.getEditor().getProject(), result);
                break;
            case MATH:
                addMathCommands(result);
                addCustomCommands(parameters.getEditor().getProject(), result);
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

    private void addCustomCommands(Project project, CompletionResultSet result) {
        Collection<LatexCommands> cmds = LatexCommandsIndex.getIndexedCommandsByName("newcommand", project);

        for (LatexCommands cmd : cmds) {
            List<String> required = cmd.getRequiredParameters();
            List<String> optional = cmd.getOptionalParameters();

            if (required.isEmpty()) {
                continue;
            }

            String cmdName = required.get(0);
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

            result.addElement(LookupElementBuilder.create(cmd, cmdName.substring(1))
                    .withPresentableText(cmdName)
                    .bold()
                    .withTailText(tailText, true)
                    .withInsertHandler(new LatexCommandArgumentInsertHandler())
                    .withIcon(TexifyIcons.DOT_COMMAND)
            );
        }
    }
}
