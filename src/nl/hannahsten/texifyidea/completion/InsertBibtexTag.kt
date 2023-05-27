package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.nextSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.previousSiblingIgnoreWhitespace
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class InsertBibtexTag : EnterHandlerDelegate {

    override fun postProcessEnter(file: PsiFile, editor: Editor, context: DataContext): Result {
        if (file.fileType != BibtexFileType) {
            return Result.Continue
        }

        val caret = editor.caretModel
        val element = file.findElementAt(caret.offset)
        if (hasValidContext(element, editor)) {
            startTemplate(file, editor)
        }

        return Result.Continue
    }

    override fun preprocessEnter(file: PsiFile, editor: Editor, p2: Ref<Int>, p3: Ref<Int>, context: DataContext, p5: EditorActionHandler?): Result {
        return Result.Continue
    }

    /**
     * Starts the tag insertion template process.
     */
    private fun startTemplate(file: PsiFile, editor: Editor) {
        val template = TemplateImpl("", "\$__Variable0\$ = {\$__Variable1\$},", "")
        template.addVariable(TextExpression("key"), true)
        template.addVariable(TextExpression("value"), true)

        val templateManager = TemplateManager.getInstance(file.project)
        templateManager.startTemplate(editor, template)
    }

    /**
     * Checks whether the element is in such a position where the tag template must be invoked.
     *
     * @return `true` when in valid context, `false` when nothing should happen or when the element is `null`.
     */
    private fun hasValidContext(element: PsiElement?, editor: Editor): Boolean {
        if (element == null) {
            return false
        }

        // Ignore @string and @preamble
        val parent = element.parentOfType(BibtexEntry::class)
        if (parent != null) {
            val token = parent.tokenName().lowercase(Locale.getDefault())
            if (token == "string" || token == "preamble") {
                return false
            }
        }

        // Check context.
        val next = element.nextSiblingIgnoreWhitespace()
        if (next is BibtexTag || next is BibtexEndtry) {
            return true
        }

        val previous = element.previousSiblingIgnoreWhitespace() ?: return false
        if (previous is BibtexId) {
            return true
        }

        // Check when no prior tags existed in the entry.
        val noTags = (
            previous is LeafPsiElement && next is LeafPsiElement &&
                previous.elementType == BibtexTypes.SEPARATOR && next.elementType == BibtexTypes.CLOSE_BRACE
            )
        if (!noTags) {
            return false
        }

        // Insert indentation if needed.
        val caret = editor.caretModel
        val file = element.containingFile
        val document = file.document() ?: return true
        val lineNumber = document.getLineNumber(caret.offset)
        val indent = document.lineIndentation(lineNumber)
        if (indent.length < 4) {
            document.insertString(caret.offset, " ".repeat(4 - indent.length))
        }

        return true
    }
}