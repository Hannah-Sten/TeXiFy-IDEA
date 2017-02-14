package nl.rubensten.texifyidea.insight;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import nl.rubensten.texifyidea.file.LatexFile;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexEnterBetweenBracesHandler extends EnterHandlerDelegateAdapter {

    @Override
    public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull
            Ref<Integer> caretOffset, @NotNull Ref<Integer> caretAdvance, @NotNull DataContext
            dataContext, EditorActionHandler originalHandler) {
        if (!(file instanceof LatexFile)) {
            return Result.Continue;
        }

        int offset = caretOffset.get();
        PsiElement before = file.findElementAt(offset - 1);
        PsiElement after = file.findElementAt(offset + 1);

        if (before == null || after == null) {
            return Result.Continue;
        }

        if (before.getNode().getElementType() == LatexTypes.DISPLAY_MATH_START && after.getNode().getElementType() == LatexTypes.DISPLAY_MATH_END) {
            originalHandler.execute(editor, editor.getCaretModel().getCurrentCaret(), dataContext);
            PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
            try {
                CodeStyleManager.getInstance(file.getProject()).adjustLineIndent(file, editor.getCaretModel().getOffset());
            }
            catch (IncorrectOperationException e) {
                Logger.getInstance(getClass()).error(e);
            }
            return Result.DefaultForceIndent;
        }

        return Result.Continue;
    }
}
