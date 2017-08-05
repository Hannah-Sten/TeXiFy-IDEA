package nl.rubensten.texifyidea.action;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexPsiUtil;
import nl.rubensten.texifyidea.psi.LatexTypes;

/**
 * @author Ruben Schellekens
 */
public class LatexToggleStarAction extends EditorAction {

    public LatexToggleStarAction() {
        super("Toggle Star", null);
    }

    @Override
    public void actionPerformed(VirtualFile file, Project project, TextEditor textEditor) {
        PsiElement element = getElement(file, project, textEditor);
        Editor editor = textEditor.getEditor();
        PsiFile psiFile = getPsiFile(file, project);

        LatexCommands commands = LatexPsiUtil.getParentOfType(element, LatexCommands.class);
        if (commands == null) {
            return;
        }

        runWriteAction(project, () -> toggleStar(editor, psiFile, commands));
    }

    /**
     * Removes the star from a latex commands or adds it when there was no star in the first place.
     *
     * @param editor
     *         The current editor.
     * @param psiFile
     *         The current file.
     * @param commands
     *         The latex command to toggle the star of.
     */
    private void toggleStar(Editor editor, PsiFile psiFile, LatexCommands commands) {
        if (removeStar(commands)) {
            return;
        }

        addStar(editor, psiFile, commands);
    }

    /**
     * Removes the star from a LaTeX command.
     *
     * @param commands
     *         The command to remove the star from.
     * @return {@code true} when the star was removed, {@code false} when the star was not removed.
     */
    private boolean removeStar(LatexCommands commands) {
        final PsiElement lastChild = commands.getLastChild();
        for (PsiElement elt = commands.getFirstChild(); elt != lastChild && elt != null; elt = elt.getNextSibling()) {
            if (!(elt instanceof LeafPsiElement)) {
                continue;
            }

            if (((LeafPsiElement)elt).getElementType() == LatexTypes.STAR) {
                elt.delete();
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a star to a latex command.
     *
     * @param editor
     *         The current editor.
     * @param file
     *         The current file.
     * @param commands
     *         The latex command to add a star to.
     */
    private void addStar(Editor editor, PsiFile file, LatexCommands commands) {
        final Document document = editor.getDocument();
        int position = editor.getCaretModel().getOffset();
        while (position > 0) {
            final String text = document.getText(new TextRange(position, position + 1));
            final PsiElement elt = file.findElementAt(position);
            final PsiElement parent = LatexPsiUtil.getParentOfType(elt, LatexCommands.class);

            if (text.equalsIgnoreCase("\\") && (elt == null || parent == commands)) {
                document.insertString(position + commands.getCommandToken().getText().length(), "*");
                return;
            }

            position--;
        }
    }
}