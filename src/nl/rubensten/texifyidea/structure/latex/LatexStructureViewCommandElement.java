package nl.rubensten.texifyidea.structure.latex;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.structure.EditableHintPresentation;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static LatexStructureViewCommandElement newCommand(LatexCommands commands) {
        if ("\\let".equals(commands.getCommandToken().getText()) ||
                "\\def".equals(commands.getCommandToken().getText())) {
            LatexCommands sibling = TexifyUtil.getNextCommand(commands);
            if (sibling == null) {
                return null;
            }

            return new LatexStructureViewCommandElement(sibling);
        }

        return new LatexStructureViewCommandElement(commands);
    }

    public void addChild(TreeElement child) {
        sectionChildren.add(child);
    }

    public String getCommandName() {
        return element.getCommandToken().getText();
    }

    public void setHint(String hint) {
        if (presentation instanceof EditableHintPresentation) {
            ((EditableHintPresentation)presentation).setHint(hint);
        }
    }

    @Override
    public Object getValue() {
        return element;
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        String text = presentation.getPresentableText();
        return text == null ? "" : text;
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
