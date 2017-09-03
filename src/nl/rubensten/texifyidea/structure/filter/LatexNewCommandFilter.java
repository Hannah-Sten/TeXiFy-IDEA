package nl.rubensten.texifyidea.structure.filter;

import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.structure.LatexOtherCommandPresentation;
import nl.rubensten.texifyidea.structure.LatexStructureViewCommandElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexNewCommandFilter implements Filter {

    @Override
    public boolean isVisible(TreeElement treeElement) {
        if (!(treeElement instanceof LatexStructureViewCommandElement)) {
            return true;
        }

        LatexStructureViewCommandElement element = (LatexStructureViewCommandElement)treeElement;
        return !(element.getCommandName().equals("\\newcommand") ||
                element.getCommandName().equals("\\DeclareMathOperator") ||
                element.getPresentation() instanceof LatexOtherCommandPresentation);
    }

    @Override
    public boolean isReverted() {
        return true;
    }

    @NotNull
    @Override
    public ActionPresentation getPresentation() {
        return LatexNewCommandFilterPresentation.INSTANCE;
    }

    @NotNull
    @Override
    public String getName() {
        return "latex.texify.filter.newcommand";
    }

    /**
     * @author Ruben Schellekens
     */
    private static class LatexNewCommandFilterPresentation implements ActionPresentation {

        private static final LatexNewCommandFilterPresentation INSTANCE = new LatexNewCommandFilterPresentation();

        @NotNull
        @Override
        public String getText() {
            return "Show Command Definitions";
        }

        @Override
        public String getDescription() {
            return "Show Command Definitions";
        }

        @Override
        public Icon getIcon() {
            return TexifyIcons.DOT_COMMAND;
        }
    }
}
