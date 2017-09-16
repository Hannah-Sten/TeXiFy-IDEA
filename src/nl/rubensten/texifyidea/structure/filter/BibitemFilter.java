package nl.rubensten.texifyidea.structure.filter;

import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.structure.latex.LatexStructureViewCommandElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class BibitemFilter implements Filter {

    @Override
    public boolean isVisible(TreeElement treeElement) {
        if (!(treeElement instanceof LatexStructureViewCommandElement)) {
            return true;
        }

        LatexStructureViewCommandElement element = (LatexStructureViewCommandElement)treeElement;
        return !element.getCommandName().equals("\\bibitem");
    }

    @Override
    public boolean isReverted() {
        return true;
    }

    @NotNull
    @Override
    public ActionPresentation getPresentation() {
        return BibitemFilterPresentation.INSTANCE;
    }

    @NotNull
    @Override
    public String getName() {
        return "latex.texify.filter.bibitem";
    }

    /**
     * @author Ruben Schellekens
     */
    private static class BibitemFilterPresentation implements ActionPresentation {

        private static final BibitemFilterPresentation INSTANCE = new BibitemFilterPresentation();

        @NotNull
        @Override
        public String getText() {
            return "Show Bibliography Items";
        }

        @Override
        public String getDescription() {
            return "Show Bibliography Items";
        }

        @Override
        public Icon getIcon() {
            return TexifyIcons.DOT_BIB;
        }
    }
}
