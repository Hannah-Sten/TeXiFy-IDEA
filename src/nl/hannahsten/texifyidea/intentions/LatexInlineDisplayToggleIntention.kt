package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.editor.MathEnvironmentEditor
import nl.hannahsten.texifyidea.psi.LatexDisplayMath
import nl.hannahsten.texifyidea.psi.LatexInlineMath
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.parser.hasParent
import nl.hannahsten.texifyidea.util.parser.parentOfType

/**
 * @author Hannah Schellekens
 */
open class LatexInlineDisplayToggleIntention : TexifyIntentionBase("Toggle inline/display math mode") {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        return element.hasParent(LatexInlineMath::class) || element.hasParent(LatexDisplayMath::class)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val inline = element.parentOfType(LatexInlineMath::class)
        if (inline != null) {
            MathEnvironmentEditor("inline", "display", editor, inline).apply()
        }
        else {
            val display = element.parentOfType(LatexDisplayMath::class) ?: return
            MathEnvironmentEditor("display", "inline", editor, display).apply()
        }
    }
}
