package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexDisplayMath
import nl.hannahsten.texifyidea.psi.LatexInlineMath
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.isLatexFile

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
            applyForInlineMath(editor, inline)
        }
        else {
            applyForDisplayMath(editor, element.parentOfType(LatexDisplayMath::class) ?: return)
        }
    }

    private fun applyForInlineMath(editor: Editor, inline: LatexInlineMath) {
        val document = editor.document
        val indent = document.lineIndentationByOffset(inline.textOffset)
        val endLength = inline.inlineMathEnd?.textLength ?: 0
        val text = inline.text.trimRange(inline.inlineMathStart.textLength, endLength).trim()

        runWriteAction {
            val extra = if (document.getText(TextRange.from(inline.endOffset(), 1)) == " ") {
                1
            }
            else 0

            val result = "\n\\[\n    $text\n\\]\n".replace("\n", "\n$indent")
            document.replaceString(inline.textOffset, inline.endOffset() + extra, result)
            editor.caretModel.moveToOffset(inline.textOffset + result.length)
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
