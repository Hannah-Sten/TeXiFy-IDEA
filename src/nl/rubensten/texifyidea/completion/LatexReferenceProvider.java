package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.TexifyIcons;
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
        Editor editor = parameters.getEditor();
        Project project = editor.getProject();
        Document document = editor.getDocument();

        Collection<LatexCommands> cmds = LatexCommandsIndex.getIndexedCommandsByName("label", project);

        for (LatexCommands commands : cmds) {
            int line = document.getLineNumber(commands.getTextOffset()) + 1;
            String typeText = commands.getContainingFile().getName() + ":" + line;

            for (String label : commands.getRequiredParameters()) {
                result.addElement(LookupElementBuilder.create(label)
                        .withPresentableText(label)
                        .bold()
                        .withTypeText(typeText, true)
                        .withInsertHandler(new LatexReferenceInsertHandler())
                        .withIcon(TexifyIcons.DOT_LABEL)
                );
            }
        }

        result.addLookupAdvertisement(Kindness.getKindWords());
    }
}
