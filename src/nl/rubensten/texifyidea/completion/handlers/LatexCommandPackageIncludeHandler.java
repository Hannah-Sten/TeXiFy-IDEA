package nl.rubensten.texifyidea.completion.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.lang.LatexCommand;
import nl.rubensten.texifyidea.lang.Package;
import nl.rubensten.texifyidea.util.PackageUtils;

import java.util.Collection;

/**
 * @author Ruben Schellekens
 */
public class LatexCommandPackageIncludeHandler implements InsertHandler<LookupElement> {

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement item) {
        LatexCommand command = (LatexCommand)item.getObject();
        Project project = insertionContext.getProject();
        Editor editor = insertionContext.getEditor();
        Document document = editor.getDocument();
        PsiFile file = insertionContext.getFile();

        if (project == null || editor == null || document == null || command == null || file == null) {
            return;
        }

        Package pack = command.getDependency();
        if (Package.DEFAULT.equals(pack)) {
            return;
        }

        Collection<String> includedPackages = PackageUtils.getIncludedPackages(file);
        if (!includedPackages.contains(pack.getName())) {
            PackageUtils.insertUsepackage(
                    insertionContext.getDocument(),
                    file,
                    pack.getName(),
                    StringUtil.join(pack.getParameters(), ",")
            );
        }
    }
}
