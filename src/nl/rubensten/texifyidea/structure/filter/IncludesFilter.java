package nl.rubensten.texifyidea.structure.filter;

import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.structure.LatexStructureViewCommandElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class IncludesFilter implements Filter {

    @Override
    public boolean isVisible(TreeElement treeElement) {
        if (!(treeElement instanceof LatexStructureViewCommandElement)) {
            return true;
        }

        LatexStructureViewCommandElement element = (LatexStructureViewCommandElement)treeElement;
        return !element.getCommandName().equals("\\include") &&
                !element.getCommandName().equals("\\includeonly") &&
                !element.getCommandName().equals("\\input");
    }

    @Override
    public boolean isReverted() {
        return true;
    }

    @NotNull
    @Override
    public ActionPresentation getPresentation() {
        return LatexIncludesFilterPresentation.INSTANCE;
    }

    @NotNull
    @Override
    public String getName() {
        return "latex.texify.filter.includes";
    }

    /**
     * @author Ruben Schellekens
     */
    private static class LatexIncludesFilterPresentation implements ActionPresentation {

        private static final LatexIncludesFilterPresentation INSTANCE = new LatexIncludesFilterPresentation();

        @NotNull
        @Override
        public String getText() {
            return "Show Includes";
        }

        @Override
        public String getDescription() {
            return "Show Includes";
        }

        @Override
        public Icon getIcon() {
            return TexifyIcons.DOT_INCLUDE;
        }
    }
}
