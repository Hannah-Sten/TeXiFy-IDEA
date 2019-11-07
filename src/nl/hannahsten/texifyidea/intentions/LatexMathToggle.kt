package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.editor.MathEnvironmentEditor
import nl.hannahsten.texifyidea.ui.MathEnvironmentDialog
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexDisplayMath
import nl.hannahsten.texifyidea.psi.LatexInlineMath
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.isLatexFile

/**
 * @author Hannah Schellekens, Abby Berkers
 */
open class LatexMathToggle : TexifyIntentionBase("Convert to other math environment") {

    /**
     * Checks if the intention is available, i.e., it is inline/display math, or one of the other math environments.
     */
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false
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
                element = element.parentOfType(LatexDisplayMath::class) ?: return
                "display"
            }
            else -> {
                element = element.findOuterMathEnvironment() ?: return
                element.childrenOfType<LatexBeginCommand>().first().environmentName()
            }
        } ?: return

        // Ask for the new environment name.
        val newEnvironmentName = MathEnvironmentDialog(environmentName).result ?: return
        // Apply the new environment.
        MathEnvironmentEditor(environmentName, newEnvironmentName, editor, element).apply()
    }
}