package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.util.Kindness;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens
 */
public class LatexReferenceProvider extends CompletionProvider<CompletionParameters> {

    LatexReferenceProvider() {
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context, @NotNull CompletionResultSet result) {
        result.addElement(LookupElementBuilder.create("thingy"));
        result.addElement(LookupElementBuilder.create("that"));
        result.addElement(LookupElementBuilder.create("bison"));

        result.addLookupAdvertisement(Kindness.getKindWords());
    }
}
