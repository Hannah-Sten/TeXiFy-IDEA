package nl.rubensten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.isLatexFile
import nl.rubensten.texifyidea.util.parentOfType

open class LatexLabelDefiningNewCommand : TexifyIntentionBase("Add label defining command to list") {
    override fun startInWriteAction(): Boolean = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false
        val selected = element as? LatexCommands ?: element.parentOfType(LatexCommands::class) ?: return false
        if (selected.name != "\\label") {
            return false
        }
        val parent = selected.parentOfType(LatexCommands::class) ?: return false
        return parent.name == "\\newcommand"
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}