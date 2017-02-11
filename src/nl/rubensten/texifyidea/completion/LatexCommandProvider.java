package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexMode;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
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
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext
            context, @NotNull CompletionResultSet result) {
        switch (mode) {
            case NORMAL:
                result.addAllElements(ContainerUtil.map2List(
                        LatexNoMathCommand.values(),
                        cmd -> LookupElementBuilder.create(cmd.getCommandDisplay())
                                .bold()
                                .withTailText(cmd.getArgumentsDisplay(), true)
                                .withTypeText(cmd.getDisplay())
                ));
            case MATH:
                result.addAllElements(ContainerUtil.map2List(
                        LatexMathCommand.values(),
                        cmd -> LookupElementBuilder.create(cmd.getCommandDisplay())
                        .bold()
                        .withTypeText(cmd.getDisplay())
                ));
        }

        result.addLookupAdvertisement("Don't use \\\\ outside of tabular or math mode, it's evil.");
    }
}
