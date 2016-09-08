package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.LatexLanguage;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexCompletionContributor extends CompletionContributor {
    public LatexCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN).withLanguage(LatexLanguage.INSTANCE),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                                              ProcessingContext context, @NotNull CompletionResultSet result) {
                    result.addElement(
                            LookupElementBuilder.create("\\begin").bold()
                                    .appendTailText("{environment}", false)
                                    .withInsertHandler((context1, item) -> {
                                        context1.getEditor().getDocument().replaceString(context1.getStartOffset() - 1, context1.getTailOffset(), item.getLookupString() + "{}");
                                    })
                    );
                }
            }
        );
    }
}
