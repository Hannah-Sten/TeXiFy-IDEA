package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.psi.util.PsiUtilCore
import nl.hannahsten.texifyidea.psi.LatexMagicComment
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.get

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
        val element = file.findElementAt(caretOffset.get())
        isComment = element is PsiComment || element?.parent is LatexMagicComment
        return super.preprocessEnter(file, editor, caretOffset, caretAdvance, dataContext, originalHandler)
    }

    override fun postProcessEnter(file: PsiFile, editor: Editor, dataContext: DataContext): EnterHandlerDelegate.Result {
        if (isComment) {
            handleEnterInComment(file, editor)
        }
        return super.postProcessEnter(file, editor, dataContext)
    }

    /**
     * Insert extra % sign when enter was pressed in a comment.
     */
    private fun handleEnterInComment(file: PsiFile, editor: Editor) {
        // Check location of the % on the previous line, especially the number of spaces before and after it
        // because that's what we want to have on the current line as well
        // (note the cursor is on the second line, after the enter has been processed)
        val currentCaret = editor.caretModel.currentCaret

        if (!currentCaret.isValid) return
        try {
            PsiUtilCore.ensureValid(file)
        }
        catch (e: PsiInvalidElementAccessException) {
            return
        }

        val previousLineNumber = (currentCaret.logicalPosition.line - 1).coerceAtLeast(0)
        val lineStart = editor.document.getLineStartOffset(previousLineNumber)
        val lineEnd = editor.document.getLineEndOffset(previousLineNumber)
        val previousLine = editor.document[IntRange(lineStart, lineEnd)]

        // If we pressed enter right before or after the %, then don't do anything, as we're not actually splitting a comment
        // To clarify the second case: consider wanting to type a % at the end of a line
        if (!previousLine.contains("%") || previousLine.endsWith("%")) {
            return
        }

        // Since the enter handler already inserted the indent of the previous line, subtract it
        val numberOfSpacesBeforePercent = previousLine.takeWhile { it == ' ' }.length
        val numberOfSpacesAfterPercent = previousLine.dropWhile { it == ' ' }
            // ! for magic comments
            .dropWhile { it == '%' || it == '!' }
            .takeWhile { it == ' ' }.length

        // Include magic comments
        val commentPrefix = """^\s*(?<percent>[%!]+)""".toRegex().find(previousLine)?.groups?.get("percent")?.value ?: return

        if (numberOfSpacesBeforePercent == 0) {
            // It was right at the start of the line, so we only have to insert it there again
            // and insert the spaces that were probably not part of the indent (which is already done by the enter handler)
            val startOfLine = editor.caretModel.currentCaret.visualLineStart
            file.document()?.insertString(startOfLine, commentPrefix + " ".repeat(numberOfSpacesAfterPercent % 4))
        }
        else {
            // There was indent before it, but that's handled by the enter handler already
            // So we just need to insert any spaces after it
            file.document()?.insertString(editor.caretModel.currentCaret.offset, commentPrefix + " ".repeat(numberOfSpacesAfterPercent))
        }
        // Alternatively, consider the setting CodeStyle.getLanguageSettings(file).LINE_COMMENT_AT_FIRST_COLUMN

        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
    }
}