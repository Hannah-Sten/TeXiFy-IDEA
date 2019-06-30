package nl.hannahsten.texifyidea.structure.filter;

import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Hannah Schellekens
 */
public class LabelFilter implements Filter {

    @Override
    public boolean isVisible(TreeElement treeElement) {
        if (!(treeElement instanceof LatexStructureViewCommandElement)) {
            return true;
        }

        LatexStructureViewCommandElement element = (LatexStructureViewCommandElement)treeElement;
        return !element.getCommandName().equals("\\label");
    }

    @Override
    public boolean isReverted() {
        return true;
    }

    @NotNull
    @Override
    public ActionPresentation getPresentation() {
        return LatexLabelFilterPresentation.INSTANCE;
    }

    @NotNull
    @Override
    public String getName() {
        return "latex.texify.filter.label";
    }

    /**
     * @author Hannah Schellekens
     */
    private static class LatexLabelFilterPresentation implements ActionPresentation {

        private static final LatexLabelFilterPresentation INSTANCE = new LatexLabelFilterPresentation();

        @NotNull
        @Override
        public String getText() {
            return "Show Labels";
        }

        @Override
        public String getDescription() {
            return "Show Labels";
        }

        @Override
        public Icon getIcon() {
            return TexifyIcons.DOT_LABEL;
        }
    }
}
