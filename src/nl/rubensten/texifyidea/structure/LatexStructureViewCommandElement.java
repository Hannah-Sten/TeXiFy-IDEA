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
public class LatexStructureViewCommandElement implements StructureViewTreeElement, SortableTreeElement {

    private final LatexCommands element;
    private final List<TreeElement> sectionChildren = new ArrayList<>();
    private final ItemPresentation presentation;

    public LatexStructureViewCommandElement(LatexCommands element) {
        this.element = element;
        this.presentation = LatexPresentationFactory.getPresentation(element);
    }

    public void addChild(TreeElement child) {
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
        return presentation.getPresentableText();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return presentation;
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
