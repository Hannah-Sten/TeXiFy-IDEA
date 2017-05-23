package nl.rubensten.texifyidea.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexStructureViewSectionElement implements StructureViewTreeElement, SortableTreeElement {


    private final LatexCommands element;
    private final List<LatexStructureViewSectionElement> sectionChildren = new ArrayList<>();

    public LatexStructureViewSectionElement(LatexCommands element) {
        this.element = element;
    }

    public void addSectionChild(LatexStructureViewSectionElement child) {
        sectionChildren.add(child);
    }

    public String getCommandName() {
        return element.getCommandToken().getText();
    }

    @Override
    public Object getValue() {
        return element;
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        return element.getName();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return LatexPresentationFactory.getPresentation(element);
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        return sectionChildren.toArray(new TreeElement[sectionChildren.size()]);
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (element instanceof NavigationItem) {
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
}
