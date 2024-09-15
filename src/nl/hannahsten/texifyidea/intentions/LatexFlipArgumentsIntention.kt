package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.getAllRequiredArguments
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.requiredParameters
import nl.hannahsten.texifyidea.util.replaceString

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

        val tokens = getToken(file, editor)?.requiredParameters() ?: return

        assert(tokens.size == 2) { "Expected to have only 2 args!" }

        val range = TextRange(tokens[0].startOffset, tokens[1].endOffset)

        val document = editor.document
        val replacements = tokens[1].text + tokens[0].text

        runWriteAction {
            document.replaceString(range, replacements)
        }
    }
}