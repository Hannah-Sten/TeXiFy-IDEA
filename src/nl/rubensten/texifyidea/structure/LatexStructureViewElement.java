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

import java.util.*;

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
        List<TreeElement> treeElements = new ArrayList<>();

        Deque<LatexStructureViewSectionElement> sections = new ArrayDeque<>();

        for (LatexCommands currentCmd : commands) {
            String token = currentCmd.getCommandToken().getText();

            // Only consider section markers.
            if (!SECTION_MARKERS.contains(token)) {
                continue;
            }

            LatexStructureViewSectionElement child = new LatexStructureViewSectionElement(currentCmd);

            // First section.
            if (sections.isEmpty()) {
                sections.addFirst(child);
                treeElements.add(child);
                continue;
            }

            int currentIndex = order(current(sections));
            int nextIndex = order(currentCmd);

            // Same level.
            if (currentIndex == nextIndex) {
                registerSameLevel(sections, child, currentCmd, treeElements);
            }
            // Go deeper
            else if (nextIndex > currentIndex) {
                registerDeeper(sections, child);
            }
            // Go higher
            else {
                registerHigher(sections, child, currentCmd, treeElements);
            }
        }

        addNewCommands(treeElements, commands);

        return treeElements.toArray(new TreeElement[treeElements.size()]);
    }

    private void addNewCommands(List<TreeElement> treeElements, List<LatexCommands> commands) {
        for (LatexCommands cmd : commands) {
            if (!cmd.getCommandToken().getText().equals("\\newcommand")) {
                continue;
            }

            List<String> required = cmd.getRequiredParameters();
            if (required.isEmpty()) {
                continue;
            }

            treeElements.add(new LatexStructureViewElement(cmd));
        }
    }

    private void registerHigher(Deque<LatexStructureViewSectionElement> sections,
                                LatexStructureViewSectionElement child,
                                LatexCommands currentCmd,
                                List<TreeElement> treeElements) {
        int indexInsert = order(currentCmd);
        while (!sections.isEmpty()) {
            pop(sections);
            int index = order(current(sections));

            if (index == indexInsert) {
                registerSameLevel(sections, child, currentCmd, treeElements);
                break;
            }

            if (indexInsert > index) {
                registerDeeper(sections, child);
                break;
            }
        }
    }

    private void registerDeeper(Deque<LatexStructureViewSectionElement> sections,
                                LatexStructureViewSectionElement child) {
        current(sections).addSectionChild(child);
        queue(child, sections);
    }

    private void registerSameLevel(Deque<LatexStructureViewSectionElement> sections,
                                   LatexStructureViewSectionElement child,
                                   LatexCommands currentCmd,
                                   List<TreeElement> treeElements) {
        sections.removeFirst();
        LatexStructureViewSectionElement parent = sections.peekFirst();
        if (parent != null) {
            parent.addSectionChild(child);
        }
        sections.addFirst(child);

        if (currentCmd.getCommandToken().getText().equals("\\section")) {
            treeElements.add(child);
        }
    }

    private void pop(Deque<LatexStructureViewSectionElement> sections) {
        sections.removeFirst();
    }

    private void queue(LatexStructureViewSectionElement child,
                       Deque<LatexStructureViewSectionElement> sections) {
        sections.addFirst(child);
    }

    private LatexStructureViewSectionElement current(Deque<LatexStructureViewSectionElement> sections) {
        return sections.getFirst();
    }

    private int order(LatexStructureViewSectionElement element) {
        return order(element.getCommandName());
    }

    private int order(LatexCommands commands) {
        return order(commands.getCommandToken().getText());
    }

    private int order(String commandName) {
        return SECTION_MARKERS.indexOf(commandName);
    }
}
