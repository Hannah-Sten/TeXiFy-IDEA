package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexDisplayMathIntention : TexifyIntentionBase("Change equation*/displaymath environment to '\\[..\\]'") {

    companion object {

        private val affectedEnvironments = setOf("displaymath", "equation*")
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val selected = file.findElementAt(editor.caretModel.offset) ?: return false
        val begin = selected.parentOfType(LatexBeginCommand::class) ?: return false
        val name = begin.environmentName() ?: return false

        return affectedEnvironments.contains(name)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        val selected = file.findElementAt(editor.caretModel.offset) ?: return
        val begin = selected.parentOfType(LatexBeginCommand::class) ?: return
        val end = begin.endCommand() ?: return

        val document = editor.document
        runWriteAction {
            document.replaceString(end.textRange, "\\]")
            document.replaceString(begin.textRange, "\\[")
        }
    }
}