package nl.hannahsten.texifyidea.editor.surroundwith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.endOffset

/**
 * Surrounds selected text with the given strings.
 *
 * @param displayBefore 'Before' string to display in popup.
 */
open class LatexSurrounder(private val before: String, private val after: String, val displayBefore: String = before, val displayAfter: String = after) : Surrounder {

    override fun isApplicable(elements: Array<out PsiElement>): Boolean {
        return true
    }

    /**
     * Puts [before] before the first element of [elements], and puts [after]
     * after the last element of [elements].
     *
     * [elements] contains only the first and last psi element in the to be surrounded range.
     *
     * @return range to select/to position the caret
     */
    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
        LatexPsiHelper(project).createFromText(before).children
            .map { it.node }
            .forEach { elements.first().parent.node.addChild(it, elements.first().node) }
        LatexPsiHelper(project).createFromText(after).children
            .map { it.node }.reversed() // Insert before the (changing) nextSibling
            .forEach { elements.last().parent.node.addChild(it, elements.last().nextSibling?.node) }

        val endOffset = elements.last().endOffset() + after.length
        return TextRange(endOffset, endOffset)
    }

    override fun getTemplateDescription(): String {
        return "Surround with $displayBefore...$displayAfter"
    }
}

open class LatexPairSurrounder(surroundPair: Pair<String, String>) : LatexSurrounder(surroundPair.first, surroundPair.second)