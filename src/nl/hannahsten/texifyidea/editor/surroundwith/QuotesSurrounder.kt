package nl.hannahsten.texifyidea.editor.surroundwith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.util.endOffset
import nl.hannahsten.texifyidea.util.getOpenAndCloseQuotes
import nl.hannahsten.texifyidea.util.insertAndMove

/**
 * Surrounder to surround a selection with opening and closing quotes, dependent
 * on the user setting.
 *
 * @author Abby Berkers
 */
open class QuotesSurrounder(val char: Char) : Surrounder {
    val quotes by lazy { getOpenAndCloseQuotes(char) }

    override fun isApplicable(elements: Array<out PsiElement>): Boolean {
        return true
    }

    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
        val startOffset = elements.first().textOffset
        editor.insertAndMove(startOffset, quotes.first)
        val endOffset = elements.last().endOffset() + quotes.first.length
        editor.insertAndMove(endOffset, quotes.second)
        return TextRange(startOffset, endOffset + quotes.second.length)
    }

    override fun getTemplateDescription(): String {
        return "Surround with ${quotes.first}${quotes.second}"
    }

    class DoubleQuotesSurrounder : QuotesSurrounder('"')
    class SingleQuotesSurrounder : QuotesSurrounder('\'')
}