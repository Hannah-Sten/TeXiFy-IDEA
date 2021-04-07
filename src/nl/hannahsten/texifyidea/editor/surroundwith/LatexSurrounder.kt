package nl.hannahsten.texifyidea.editor.surroundwith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.endOffset

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
     * [elements] contains only the first and last psi element in the to be surrounded range.
     */
    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
        val beforeNode = LatexPsiHelper(project).createFromText(before).firstChild.node
        val afterNode = LatexPsiHelper(project).createFromText(after).firstChild.node
        elements.first().parent.node.addChild(beforeNode, elements.first().node)
        elements.last().parent.node.addChild(afterNode, elements.last().nextSibling?.node)
        // Reparse the document because this introduces parse errors :(
        PsiDocumentManager.getInstance(project).reparseFiles(mutableSetOf(elements.first().containingFile.virtualFile), true)
        // Not sure why the -1 is needed...
        val endOffset = elements.last().endOffset() + before.length + after.length - 1
        return TextRange(endOffset, endOffset)
    }

    override fun getTemplateDescription(): String {
        return "Surround with $before$after"
    }
}

open class LatexPairSurrounder(surroundPair: Pair<String, String>) : LatexSurrounder(surroundPair.first, surroundPair.second)