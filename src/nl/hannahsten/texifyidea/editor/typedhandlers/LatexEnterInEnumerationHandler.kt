package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.codeInsight.editorActions.AutoHardWrapHandler
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.editor.ControlTracker
import nl.hannahsten.texifyidea.editor.ShiftTracker
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

/**
 * @author Hannah Schellekens
 */
class LatexEnterInEnumerationHandler : EnterHandlerDelegate {

    override fun postProcessEnter(
        file: PsiFile, editor: Editor,
        context: DataContext
    ): Result {
        if (file.fileType != LatexFileType) {
            return Result.Continue
        }

        // Don't insert \item when the enter was triggered by the word wrap
        if (DataManager.getInstance().loadFromDataContext(context, AutoHardWrapHandler.AUTO_WRAP_LINE_IN_PROGRESS_KEY) == true) {
            return Result.Continue
        }

        ShiftTracker.setup(editor.contentComponent)
        ControlTracker.setup(editor.contentComponent)

        val caret = editor.caretModel
        val element = file.findElementAt(caret.offset)
        if (hasValidContext(element)) {
            val previousMarker = getPreviousMarker(element!!)
            if (previousMarker == null) {
                editor.insertAtCaretAndMove( "\\item ")
            }
            else {
                // Use live template, so that the user can choose to replace the label and press enter to jump out of the optional argument
                val template = TemplateImpl("", "\\item[\$__Variable0\$] ", "")
                template.addVariable(TextExpression(previousMarker.trim('[', ']')), true)
                TemplateManager.getInstance(file.project).startTemplate(editor, template)
            }
        }
        else {
            if (ControlTracker.isControlPressed) {
                editor.insertAtCaretAndMove( "")
            }
        }

        return Result.Continue
    }

    override fun preprocessEnter(
        file: PsiFile, editor: Editor, p2: Ref<Int>, p3: Ref<Int>,
        context: DataContext,
        p5: EditorActionHandler?
    ): Result {
        return Result.Continue
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
        // Only consider direct children, because there could be nested enumerations which we should ignore
        return environment.children
            .firstOrNull { it is LatexEnvironmentContent }
            ?.children
            ?.flatMap { it.children.toList() }
            ?.filterIsInstance<LatexCommands>()
            ?.lastOrNull { it.name == "\\item" }
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
        if (!TexifySettings.getInstance().automaticItemInItemize || element == null || ShiftTracker.isShiftPressed() || ControlTracker.isControlPressed || element.inMathContext()) {
            return false
        }

        val isGluedToTheBeginCommand = element.hasParent(LatexBeginCommand::class)
        val isInsideAnEnumeration = element.inDirectEnvironment(EnvironmentMagic.listingEnvironments)
        return isInsideAnEnumeration && !isGluedToTheBeginCommand
    }
}
