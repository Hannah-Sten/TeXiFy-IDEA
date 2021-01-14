package nl.hannahsten.texifyidea.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.codeInsight.editorActions.SelectWordUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * Select all of the LatexCommands, so including the backslash.
 */
class LatexCommandSelectioner : ExtendWordSelectionHandlerBase() {

    override fun canSelect(e: PsiElement): Boolean {
        return !CommandSelectionFilter().value(e)
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): MutableList<TextRange>? {
        val ranges = super.select(e, editorText, cursorOffset, editor) ?: return null
        val commandRange = e.textRange

        SelectWordUtil.addWordOrLexemeSelection(false, editor, cursorOffset, mutableListOf(commandRange)) { c: Char -> c.isLetterOrDigit() || c == '\\' }
        ranges += commandRange
        return ranges
    }
}