package nl.rubensten.texifyidea.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import nl.rubensten.texifyidea.file.LatexFile;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    public static final List<String> SECTION_MARKERS = Arrays.asList(
            "\\section", "\\subsection", "\\subsubsection", "\\paragraph", "\\subparagraph"
    );

    private PsiElement element;

    public LatexStructureViewElement(PsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (element instanceof NavigationItem) {
            System.out.println("NAVIGATE");
            ((NavigationItem)element).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return (element instanceof NavigationItem) && ((NavigationItem)element).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return (element instanceof NavigationItem) && ((NavigationItem)element).canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        return (element instanceof PsiNamedElement) ? ((PsiNamedElement)element).getName() : null;
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        if (element instanceof LatexCommands) {
            return LatexPresentationFactory.getPresentation((LatexCommands)element);
        }
        else if (element instanceof PsiFile) {
            return new LatexFilePresentation((PsiFile)element);
        }

        return null;
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        if (!(element instanceof LatexFile)) {
            return EMPTY_ARRAY;
        }

        // Fetch all commands in the active file.
        List<LatexCommands> commands = TexifyUtil.getAllCommands(element);
        List<LatexStructureViewSectionElement> treeElements = new ArrayList<>();
        LatexStructureViewSectionElement sectionElement = null;

        for (LatexCommands cmd : commands) {
            String token = cmd.getCommandToken().getText();

            if (!SECTION_MARKERS.contains(token)) {
                continue;
            }

            if (sectionElement == null || token.equals("\\section")) {
                sectionElement = new LatexStructureViewSectionElement(cmd);
                treeElements.add(sectionElement);
            }
            else {
                sectionElement.addSectionChild(new LatexStructureViewSectionElement(cmd));
            }
        }

        return treeElements.toArray(new TreeElement[treeElements.size()]);
    }
}
