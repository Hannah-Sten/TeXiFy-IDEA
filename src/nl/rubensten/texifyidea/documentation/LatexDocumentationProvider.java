package nl.rubensten.texifyidea.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sten Wessel
 */
public class LatexDocumentationProvider implements DocumentationProvider {

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement psiElement, PsiElement originalElement) {
        if (psiElement instanceof LatexCommands) {
            LatexCommands cmd = (LatexCommands)psiElement;
            if ("\\label".equals(cmd.getName())) {
                String label = cmd.getRequiredParameters().get(0);
                String file = cmd.getContainingFile().getName();
                int line = StringUtil.offsetToLineNumber(cmd.getContainingFile().getText(), cmd.getTextOffset()) + 1;  // Because line numbers do start at 1
                return String.format("Go to declaration of label '%s' [%s:%d]", label, file, line);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public List<String> getUrlFor(PsiElement psiElement, PsiElement originalElement) {
        return null;
    }

    @Nullable
    @Override
    public String generateDoc(PsiElement psiElement, @Nullable PsiElement originalElement) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object o,
                                                           PsiElement psiElement) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String s, PsiElement psiElement) {
        return null;
    }
}
