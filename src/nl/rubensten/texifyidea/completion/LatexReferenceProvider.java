package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
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

        for (String key : getKeys(project)) {
            Collection<LatexCommands> cmds = getCommandsByName(key, project);
            for (LatexCommands commands : cmds) {
                // Only accept \label commands for references.
                if (!commands.getCommandToken().getText().equals("\\label")) {
                    continue;
                }

                for (String label : commands.getRequiredParameters()) {
                    result.addElement(LookupElementBuilder.create(label));
                }
            }
        }

        result.addLookupAdvertisement(Kindness.getKindWords());
    }

    public Collection<LatexCommands> getCommandsByName(String name, Project project) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        return StubIndex.getElements(LatexCommandsIndex.KEY, name, project, scope, LatexCommands.class);
    }

    public String[] getKeys(Project project) {
        return ArrayUtil.toStringArray(StubIndex.getInstance().getAllKeys(LatexCommandsIndex.KEY, project));
    }
}
