package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.files.getAllRequiredArguments
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.requiredParameters

class LatexFlipArgumentsIntention : TexifyIntentionBase("Swap the two arguments of a command") {
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val parent = getToken(file, editor) ?: return false

        val argCount = parent.getAllRequiredArguments()?.size ?: return false

        return argCount == 2
    }

    private fun getToken(
        file: PsiFile,
        editor: Editor
    ): LatexCommands? {
        val selected = file.findElementAt(editor.caretModel.offset) ?: return null

        return selected.parentOfType(LatexCommands::class)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        val command = getToken(file, editor) ?: return
        val parameters = command.requiredParameters()

        assert(parameters.size == 2) { "Expected to have only 2 args!" }

        val firstParameterText = parameters[0].node.text
        val newParameter = LatexPsiHelper(project).createRequiredParameter(firstParameterText, hasBraces = true).node
        command.node.addChild(newParameter)
        parameters[0].delete()
    }
}