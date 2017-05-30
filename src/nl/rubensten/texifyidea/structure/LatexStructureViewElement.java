package nl.rubensten.texifyidea.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.file.LatexFile;
import nl.rubensten.texifyidea.file.LatexFileType;
import nl.rubensten.texifyidea.file.StyleFileType;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.lang.RequiredFileArgument;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexTypes;
import nl.rubensten.texifyidea.structure.SectionNumbering.DocumentClass;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static nl.rubensten.texifyidea.util.TexifyUtil.findFile;

/**
 * @author Ruben Schellekens
 */
public class LatexStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    public static final List<String> SECTION_MARKERS = Arrays.asList(
            "\\part", "\\chapter",
            "\\section", "\\subsection", "\\subsubsection", "\\paragraph", "\\subparagraph"
    );

    private final PsiElement element;

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
        if (element instanceof LatexCommands) {
            return ((LatexCommands)element).getCommandToken().getText().toLowerCase();
        }
        else if (element instanceof PsiNamedElement) {
            return ((PsiNamedElement)element).getName().toLowerCase();
        }
        else {
            return element.getText().toLowerCase();
        }
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

        // Get document class.
        GlobalSearchScope scope = GlobalSearchScope.fileScope((PsiFile)element);
        String docClass = LatexCommandsIndex.getIndexedCommands(element.getProject(), scope)
                .stream()
                .filter(cmd -> cmd.getCommandToken().getText().equals("\\documentclass") &&
                        !cmd.getRequiredParameters().isEmpty())
                .map(cmd -> cmd.getRequiredParameters().get(0))
                .findFirst()
                .orElse("article");

//        ((PsiModificationTrackerImpl)PsiManager.getInstance(element.getProject()).getModificationTracker()).incOutOfCodeBlockModificationCounter();

        // Fetch all commands in the active file.
        SectionNumbering numbering = new SectionNumbering(DocumentClass.getClassByName(docClass));
        List<LatexCommands> commands = TexifyUtil.getAllCommands(element);
        List<TreeElement> treeElements = new ArrayList<>();

        // Add includes.
        addIncludes(treeElements, commands);

        // Add sectioning.
        Deque<LatexStructureViewCommandElement> sections = new ArrayDeque<>();
        for (LatexCommands currentCmd : commands) {
            String token = currentCmd.getCommandToken().getText();

            // Update counter.
            if ((token.equals("\\addtocounter") || token.equals("\\setcounter"))) {
                updateNumbering(currentCmd, numbering);
                continue;
            }

            // Only consider section markers.
            if (!SECTION_MARKERS.contains(token)) {
                continue;
            }

            if (!docClass.equals("book") && (token.equals("\\part") || token.equals("\\chapter"))) {
                continue;
            }

            if (currentCmd.getRequiredParameters().isEmpty()) {
                continue;
            }

            LatexStructureViewCommandElement child = new LatexStructureViewCommandElement(currentCmd);

            // First section.
            if (sections.isEmpty()) {
                sections.addFirst(child);
                treeElements.add(child);
                setLevelHint(child, numbering);
                continue;
            }

            int currentIndex = order(current(sections));
            int nextIndex = order(currentCmd);

            // Same level.
            if (currentIndex == nextIndex) {
                registerSameLevel(sections, child, currentCmd, treeElements, numbering);
            }
            // Go deeper
            else if (nextIndex > currentIndex) {
                registerDeeper(sections, child, numbering);
            }
            // Go higher
            else {
                registerHigher(sections, child, currentCmd, treeElements, numbering);
            }
        }

        // Add command definitions.
        addFromCommand(treeElements, commands, "\\newcommand");

        // Add label definitions.
        addFromCommand(treeElements, commands, "\\label");

        return treeElements.toArray(new TreeElement[treeElements.size()]);
    }

    private void addIncludes(List<TreeElement> treeElements, List<LatexCommands> commands) {
        for (LatexCommands cmd : commands) {
            String name = cmd.getCommandToken().getText();
            if (!name.equals("\\include") && !name.equals("\\includeonly") && !name.equals("\\input")) {
                continue;
            }

            List<String> required = cmd.getRequiredParameters();
            if (required.isEmpty()) {
                continue;
            }

            // Find file
            Optional<LatexNoMathCommand> latexCommandHuh = LatexNoMathCommand.get(name.substring(1));
            if (!latexCommandHuh.isPresent()) {
                continue;
            }
            RequiredFileArgument argument = latexCommandHuh.get()
                    .getArgumentsOf(RequiredFileArgument.class)
                    .get(0);

            String fileName = required.get(0);
            VirtualFile directory = element.getContainingFile().getContainingDirectory().getVirtualFile();
            Optional<VirtualFile> fileHuh = findFile(directory, fileName, argument.getSupportedExtensions());
            if (!fileHuh.isPresent()) {
                continue;
            }

            PsiFile psiFile = PsiManager.getInstance(element.getProject()).findFile(fileHuh.get());
            if (!LatexFileType.INSTANCE.equals(psiFile.getFileType()) &&
                    !StyleFileType.INSTANCE.equals(psiFile.getFileType())) {
                continue;
            }

            LatexStructureViewCommandElement elt = new LatexStructureViewCommandElement(cmd);
            elt.addChild(new LatexStructureViewElement(psiFile));
            treeElements.add(elt);
        }
    }

    private void addFromCommand(List<TreeElement> treeElements, List<LatexCommands> commands,
                                String commandName) {
        for (LatexCommands cmd : commands) {
            if (!cmd.getCommandToken().getText().equals(commandName)) {
                continue;
            }

            List<String> required = cmd.getRequiredParameters();
            if (required.isEmpty()) {
                continue;
            }

            treeElements.add(new LatexStructureViewCommandElement(cmd));
        }
    }

    private void registerHigher(Deque<LatexStructureViewCommandElement> sections,
                                LatexStructureViewCommandElement child,
                                LatexCommands currentCmd,
                                List<TreeElement> treeElements,
                                SectionNumbering numbering) {
        int indexInsert = order(currentCmd);
        while (!sections.isEmpty()) {
            pop(sections);
            int index = order(current(sections));

            if (index == indexInsert) {
                registerSameLevel(sections, child, currentCmd, treeElements, numbering);
                break;
            }

            if (indexInsert > index) {
                registerDeeper(sections, child, numbering);
                break;
            }
        }
    }

    private void registerDeeper(Deque<LatexStructureViewCommandElement> sections,
                                LatexStructureViewCommandElement child,
                                SectionNumbering numbering) {
        current(sections).addChild(child);
        queue(child, sections);

        setLevelHint(child, numbering);
    }

    private void registerSameLevel(Deque<LatexStructureViewCommandElement> sections,
                                   LatexStructureViewCommandElement child,
                                   LatexCommands currentCmd,
                                   List<TreeElement> treeElements,
                                   SectionNumbering numbering) {
        sections.removeFirst();
        LatexStructureViewCommandElement parent = sections.peekFirst();
        if (parent != null) {
            parent.addChild(child);
        }
        sections.addFirst(child);

        setLevelHint(child, numbering);

        if (currentCmd.getCommandToken().getText().equals(highestLevel(sections))) {
            treeElements.add(child);
        }
    }

    private void setLevelHint(LatexStructureViewCommandElement child, SectionNumbering numbering) {
        if (hasStar((LatexCommands)child.getValue())) {
            return;
        }

        int level = order(child);
        numbering.increase(level);
        child.setHint(numbering.getTitle(level));
    }

    private void updateNumbering(LatexCommands cmd, SectionNumbering numbering) {
        String token = cmd.getCommandToken().getText();
        List<String> required = cmd.getRequiredParameters();

        if (required.size() < 2) {
            return;
        }

        // Get the level to modify.
        String name = required.get(0);
        int level = order("\\" + name);
        if (level == -1) {
            return;
        }

        // Get the amount to modify with.
        int amount = -1;
        try {
            amount = Integer.parseInt(required.get(1));
        }
        catch (NumberFormatException nfe) {
            return;
        }

        // Setcounter
        if (token.equals("\\setcounter")) {
            numbering.setCounter(level, amount);
        }
        // Addcounter
        else {
            numbering.addCounter(level, amount);
        }
    }

    private boolean hasStar(LatexCommands commands) {
        LeafPsiElement[] leafs = PsiTreeUtil.getChildrenOfType(commands, LeafPsiElement.class);
        return Arrays.stream(leafs)
                .anyMatch(l -> l.getElementType().equals(LatexTypes.STAR));
    }

    private String highestLevel(Deque<LatexStructureViewCommandElement> sections) {
        return sections.stream()
                .map(this::order)
                .min(Integer::compareTo)
                .map(SECTION_MARKERS::get)
                .orElse("\\section");
    }

    private void pop(Deque<LatexStructureViewCommandElement> sections) {
        sections.removeFirst();
    }

    private void queue(LatexStructureViewCommandElement child,
                       Deque<LatexStructureViewCommandElement> sections) {
        sections.addFirst(child);
    }

    private LatexStructureViewCommandElement current(Deque<LatexStructureViewCommandElement> sections) {
        return sections.getFirst();
    }

    private int order(LatexStructureViewCommandElement element) {
        return order(element.getCommandName());
    }

    private int order(LatexCommands commands) {
        return order(commands.getCommandToken().getText());
    }

    private int order(String commandName) {
        return SECTION_MARKERS.indexOf(commandName);
    }
}
