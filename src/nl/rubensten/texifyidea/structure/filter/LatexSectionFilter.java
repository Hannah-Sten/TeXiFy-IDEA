package nl.rubensten.texifyidea.structure.filter;

import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.structure.LatexStructureViewCommandElement;
import nl.rubensten.texifyidea.structure.LatexStructureViewElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexSectionFilter implements Filter {

    @Override
    public boolean isVisible(TreeElement treeElement) {
        if (!(treeElement instanceof LatexStructureViewCommandElement)) {
            return true;
        }

        LatexStructureViewCommandElement element = (LatexStructureViewCommandElement)treeElement;
        return !LatexStructureViewElement.SECTION_MARKERS.contains(element.getCommandName());
    }

    @Override
    public boolean isReverted() {
        return true;
    }

    @NotNull
    @Override
    public ActionPresentation getPresentation() {
        return LatexSectionFilterPresentation.INSTANCE;
    }

    @NotNull
    @Override
    public String getName() {
        return "latex.texify.filter.section";
    }

    /**
     * @author Ruben Schellekens
     */
    private static class LatexSectionFilterPresentation implements ActionPresentation {

        private static final LatexSectionFilterPresentation INSTANCE = new LatexSectionFilterPresentation();

        @NotNull
        @Override
        public String getText() {
            return "Show Sectioning";
        }

        @Override
        public String getDescription() {
            return "Show Sectioning";
        }

        @Override
        public Icon getIcon() {
            return TexifyIcons.DOT_SECTION;
        }
    }
}
