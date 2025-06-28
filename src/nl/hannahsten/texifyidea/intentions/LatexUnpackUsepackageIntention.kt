package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.requiredParameter

/**
 * @author Hannah Schellekens
 */
open class LatexUnpackUsepackageIntention : TexifyIntentionBase("Split into multiple \\usepackage commands") {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val selected = file.findElementAt(editor.caretModel.offset) ?: return false
        val command = selected.parentOfType(LatexCommands::class) ?: return false

        if (command.name != "\\usepackage") {
            return false
        }

        val required = command.requiredParameter(0)
        return required != null && required.contains(",")
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        // Find packages.
        val selected = file.findElementAt(editor.caretModel.offset) ?: return
        val command = selected.parentOfType(LatexCommands::class) ?: return
        val required = command.findFirstChildOfType(LatexRequiredParam::class) ?: return
        val requiredText = required.text.trimRange(1, 1)
        val packages = PatternMagic.parameterSplit.split(requiredText)

        // Reorganise includes.
        val document = file.document() ?: return
        val offset = command.textOffset

        runWriteAction {
            document.deleteString(offset, command.endOffset())
            for (i in packages.size - 1 downTo 0) {
                val newline = if (i > 0) "\n" else ""
                val pack = packages[i]
                document.insertString(offset, "$newline\\usepackage{$pack}")
            }
        }
    }
}