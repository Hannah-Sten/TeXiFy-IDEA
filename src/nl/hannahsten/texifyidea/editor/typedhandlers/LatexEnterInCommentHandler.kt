package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.util.files.document

class LatexEnterInCommentHandler : EnterHandlerDelegateAdapter() {

    // Used to enable the preprocess to tell the postprocess what to do
    var isComment = false

    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        isComment = file.findElementAt(caretOffset.get()) is PsiComment
        return super.preprocessEnter(file, editor, caretOffset, caretAdvance, dataContext, originalHandler)
    }

    override fun postProcessEnter(file: PsiFile, editor: Editor, dataContext: DataContext): EnterHandlerDelegate.Result {
        if (isComment) {
            // Not entirely correct, because the % sign on the previous column may have a different indent than the setting
            // But looking at the setting is much easier for now
            if (CodeStyle.getLanguageSettings(file).LINE_COMMENT_AT_FIRST_COLUMN) {
                val startOfLine = editor.caretModel.currentCaret.visualLineStart
                file.document()?.insertString(startOfLine, "%")

            }
            else {
                file.document()?.insertString(editor.caretModel.currentCaret.offset, "% ")
            }
        }
        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
        return super.postProcessEnter(file, editor, dataContext)
    }
}