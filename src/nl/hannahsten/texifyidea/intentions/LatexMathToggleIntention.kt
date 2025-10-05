package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.editor.MathEnvironmentEditor
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexDisplayMath
import nl.hannahsten.texifyidea.psi.LatexInlineMath
import nl.hannahsten.texifyidea.psi.environmentName
import nl.hannahsten.texifyidea.ui.PopupChooserCellRenderer
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.parser.*

/**
 * @author Hannah Schellekens, Abby Berkers
 */
open class LatexMathToggleIntention : TexifyIntentionBase("Convert to other math environment") {

    /**
     * Checks if the intention is available, i.e., it is inline/display math, or one of the other math environments.
     */
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset)
            ?: return false
        return element.inMathContext() || element.hasParent(LatexInlineMath::class)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        var element = file.findElementAt(editor.caretModel.offset) ?: return
        // Get the environment and its name of the (outer) math environment.
        val environmentName = when {
            element.hasParent(LatexInlineMath::class) -> {
                element = element.parentOfType(LatexInlineMath::class) ?: return
                "inline"
            }
            element.hasParent(LatexDisplayMath::class) -> {
                element = element.parentOfType(LatexDisplayMath::class)
                    ?: return
                "display"
            }
            else -> {
                element = element.findOuterMathEnvironment() ?: return
                element.findFirstChildTyped<LatexBeginCommand>()?.environmentName()
            }
        } ?: return

        val availableEnvironments: List<String> = arrayOf(
            DefaultEnvironment.entries
                .filter { it.context == Environment.Context.MATH }
                .map { it.environmentName }
                .toTypedArray(),
            // Add the inline and display environments.
            arrayOf("inline", "display")
        )
            .flatten()
            // Remove equation*/displaymath, split/cases, and current environments.
            .filter { it != "split" && it != "cases" && it != "equation*" && it != "displaymath" && it != environmentName }
            .sorted()

        // Ask for the new environment name.
        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(availableEnvironments)
            .setTitle("Math Environments")
            .setItemChosenCallback {
                // Apply the chosen environment.
                MathEnvironmentEditor(environmentName, it, editor, element).apply()
            }
            .setRenderer(PopupChooserCellRenderer())
            .createPopup().showInBestPositionFor(editor)
    }
}