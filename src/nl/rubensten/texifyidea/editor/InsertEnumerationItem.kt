package nl.rubensten.texifyidea.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.file.LatexFileType
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.psi.LatexOptionalParam
import nl.rubensten.texifyidea.util.*

/**
 * @author Ruben Schellekens
 */
class InsertEnumerationItem : EnterHandlerDelegate {

    override fun postProcessEnter(file: PsiFile, editor: Editor, context: DataContext): EnterHandlerDelegate.Result {
        ShiftTracker.setup(editor.contentComponent)
        if (file.fileType != LatexFileType) {
            return Result.Continue
        }

        val caret = editor.caretModel
        val element = file.findElementAt(caret.offset)
        if (hasValidContext(element)) {
            editor.insertAndMove(caret.offset, getInsertionString(element!!))
        }

        return Result.Continue
    }

    override fun preprocessEnter(file: PsiFile, editor: Editor, p2: Ref<Int>, p3: Ref<Int>, context: DataContext, p5: EditorActionHandler?): EnterHandlerDelegate.Result {
        return Result.Continue
    }

    /**
     * Get the string that must be inserted at the caret.
     */
    private fun getInsertionString(element: PsiElement): String {
        val marker = getPreviousMarker(element)
        return "\\item" + if (marker == null) " " else "$marker "
    }

    /**
     * Get the special marker that is used at the previous item (if any).
     */
    private fun getPreviousMarker(element: PsiElement): String? {
        val environment = element.parentOfType(LatexEnvironment::class) ?: return null

        // Last element in the list => Find last \item.
        val label = if (element.parent is LatexEnvironment) {
            getLastLabel(environment)
        }
        // Middle of the list => Find previous \item.
        else {
            getPreviousLabel(element)
        } ?: return null // when no label could befound.

        // Extract optional parameters.
        val optionals = label.childrenOfType(LatexOptionalParam::class).firstOrNull() ?: return null
        return optionals.text
    }

    /**
     * Finds the last label in the environment.
     *
     * @return The last label in the environment, or `null` when there are no labels.
     */
    private fun getLastLabel(environment: PsiElement): LatexCommands? {
        val commands = environment.childrenOfType(LatexCommands::class)
                .filter { it.name == "\\item" }
        if (commands.isEmpty()) {
            return null
        }

        return commands.last()
    }

    /**
     * Finds the label that comes before `element`.
     *
     * @return The previous label, or `null` when it couldn't be found.
     */
    private fun getPreviousLabel(element: PsiElement): LatexCommands? {
        var sibling: PsiElement? = element.previousSiblingIgnoreWhitespace()

        while (sibling != null) {
            val label = getLastLabel(sibling)
            if (label != null) {
                return label
            }

            sibling = sibling.previousSiblingIgnoreWhitespace()
        }

        return null
    }

    /**
     * Checks if insertion of `\item` is desired.
     *
     * @return `true` insertion desired, `false` insertion not desired or element is `null`.
     */
    private fun hasValidContext(element: PsiElement?): Boolean {
        if (element == null || ShiftTracker.isShiftPressed() || element.inMathContext()) {
            return false
        }

        return element.inDirectEnvironment(Magic.Environment.listingEnvironments)
    }
}