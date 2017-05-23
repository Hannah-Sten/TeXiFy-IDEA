package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.completion.handlers.LatexReferenceInsertHandler;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.Kindness;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Ruben Schellekens
 */
public class LatexReferenceProvider extends CompletionProvider<CompletionParameters> {

    LatexReferenceProvider() {
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context, @NotNull CompletionResultSet result) {
        Project project = parameters.getEditor().getProject();

        Collection<LatexCommands> cmds = LatexCommandsIndex.getIndexedCommandsByName("label", project);

        for (LatexCommands commands : cmds) {
            for (String label : commands.getRequiredParameters()) {
                result.addElement(LookupElementBuilder.create(label)
                        .withInsertHandler(new LatexReferenceInsertHandler()));
            }
        }

        result.addLookupAdvertisement(Kindness.getKindWords());
    }
}
