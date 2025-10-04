package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * In the situation \[<caret>\], when pressing enter the caret
 * should end up with the correct indent.
 *
 * @author Sten Wessel
 */
class LatexEnterBetweenBracesHandler : EnterHandlerDelegateAdapter() {

    override fun preprocessEnter(file: PsiFile, editor: Editor, caretOffset: Ref<Int>, caretAdvance: Ref<Int>, dataContext: DataContext, originalHandler: EditorActionHandler?): EnterHandlerDelegate.Result {
        if (file !is LatexFile) {
            return EnterHandlerDelegate.Result.Continue
        }

        val offset = caretOffset.get()
        val before = file.findElementAt(offset - 1)
        val after = file.findElementAt(offset + 1)

        if (before == null || after == null) {
            return EnterHandlerDelegate.Result.Continue
        }

        if (before.node.elementType === LatexTypes.DISPLAY_MATH_START && after.node.elementType === LatexTypes.DISPLAY_MATH_END) {
            originalHandler!!.execute(editor, editor.caretModel.currentCaret, dataContext)
            PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
            try {
                CodeStyleManager.getInstance(file.project).adjustLineIndent(file, editor.caretModel.offset)
            }
            catch (e: IncorrectOperationException) {
                Logger.getInstance(javaClass).error(e)
            }

            return EnterHandlerDelegate.Result.DefaultForceIndent
        }

        return EnterHandlerDelegate.Result.Continue
    }
}
