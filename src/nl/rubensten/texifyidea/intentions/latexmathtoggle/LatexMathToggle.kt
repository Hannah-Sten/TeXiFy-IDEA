package nl.rubensten.texifyidea.intentions.latexmathtoggle

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.intentions.TexifyIntentionBase
import nl.rubensten.texifyidea.lang.DefaultEnvironment
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.psi.LatexBeginCommand
import nl.rubensten.texifyidea.psi.LatexDisplayMath
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.psi.LatexInlineMath
import nl.rubensten.texifyidea.util.*
import javax.swing.*

/**
 * @author Ruben Schellekens, Abby Berkers
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
        return element.inMathContext()
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        var element = file.findElementAt(editor.caretModel.offset) ?: return
        // Get the environment name of the (outer) math environment.
        val envName = when {
            element.hasParent(LatexInlineMath::class) -> "inline"
            element.hasParent(LatexDisplayMath::class) -> "display"
            else -> {
                element = element.findOuterMathEnvironment() ?: return
                element.childrenOfType<LatexBeginCommand>().first().environmentName()
            }
        }
        val newEnvName = MathEnvironmentDialog(envName).result
        println(OneLiner(element.text).getOneLiner())
    }

    private fun applyForAlign(editor: Editor, align: LatexEnvironment) {
        val document = editor.document
        val indent = document.lineIndentationByOffset(align.textOffset)
        val text = align.text.trimRange(2, 2).trim()

        runWriteAction {
            val extra = if (document.getText(TextRange.from(align.endOffset(), 1)) == " ") {
                1
            } else 0

            val result = "\n\\[\n    $text\n\\]\n".replace("\n", "\n$indent")
            document.replaceString(align.textOffset, align.endOffset() + extra, result)
            editor.caretModel.moveToOffset(align.textOffset + result.length)
        }
    }

    private fun applyForDisplayMath(editor: Editor, display: LatexDisplayMath) {
        val document = editor.document
        val indent = document.lineIndentationByOffset(display.textOffset)
        val whitespace = indent.length + 1
        val text = display.text.trimRange(2, 2).trim()

        runWriteAction {
            val leading = if (document.getText(TextRange.from(display.textOffset - whitespace - 1, 1)) != " ") " " else ""
            val trailing = if (document.getText(TextRange.from(display.endOffset() + whitespace, 1)) != " ") " " else ""

            val result = "$leading${'$'}$text${'$'}$trailing"
                    .replace("\n", " ")
                    .replace(Regex("\\s+"), " ")
            document.replaceString(display.textOffset - whitespace, display.endOffset() + whitespace, result)
        }
    }
}