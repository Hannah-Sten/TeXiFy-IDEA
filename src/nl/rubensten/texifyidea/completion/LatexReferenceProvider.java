package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.completion.handlers.LatexReferenceInsertHandler;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.Kindness;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens
 * @deprecated From b0.3 onward. Use {@link nl.rubensten.texifyidea.reference.LatexLabelReference} instead.
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

        if (project == null) {
            return;
        }

        // Only consider included files.
        PsiFile file = parameters.getOriginalFile();
        Set<VirtualFile> searchFiles = TexifyUtil.getReferencedFiles(file).stream()
                .map(PsiFile::getVirtualFile)
                .collect(Collectors.toSet());
        searchFiles.add(file.getVirtualFile());
        GlobalSearchScope scope = GlobalSearchScope.filesScope(project, searchFiles);

        Collection<LatexCommands> cmds = LatexCommandsIndex.getIndexedCommandsByName(
                "label", project, scope
        );

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
