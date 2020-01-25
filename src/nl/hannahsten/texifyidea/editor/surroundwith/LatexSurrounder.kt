package nl.hannahsten.texifyidea.editor.surroundwith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.util.endOffset
import nl.hannahsten.texifyidea.util.insertAndMove

/**
 * Surrounds selected text with the [surroundPair].
 */
open class LatexSurrounder(private val before: String, private val after: String) : Surrounder {
    override fun isApplicable(elements: Array<out PsiElement>): Boolean {
        return true
    }

    /**
     * Puts [before] before the first element of [elements], and puts [after]
     * after the last element of [elements].
     *
     * This requires the elements to be sorted.
     */
    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
        val startOffset = elements.first().textOffset
        editor.insertAndMove(startOffset, before)
        val endOffset = elements.last().endOffset() + before.length
        editor.insertAndMove(endOffset, after)
        return TextRange(startOffset, endOffset + after.length)
    }

    override fun getTemplateDescription(): String {
        return "Surround with $before$after"
    }
}

open class LatexPairSurrounder(surroundPair: Pair<String, String>) : LatexSurrounder(surroundPair.first, surroundPair.second)