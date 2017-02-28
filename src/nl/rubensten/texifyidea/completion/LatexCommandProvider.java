package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import nl.rubensten.texifyidea.completion.handlers.LatexNoMathInsertHandler;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexMode;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathEnvironment;
import org.jetbrains.annotations.NotNull;

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
                break;
            case MATH:
                addMathCommands(result);
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
        ));
    }

    private void addEnvironments(CompletionResultSet result) {
        result.addAllElements(ContainerUtil.map2List(
                LatexNoMathEnvironment.values(),
                cmd -> LookupElementBuilder.create(cmd, cmd.getName())
                        .withPresentableText(cmd.getName())
        ));
    }
}
