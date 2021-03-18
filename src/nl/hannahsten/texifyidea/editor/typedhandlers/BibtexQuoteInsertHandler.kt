package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.BibtexFileType

/**
 * This class provides the auto-insertion of a second double quote when one double quote is typed in bibtex files.
 *
 * @author Hannah Schellekens
 */
open class BibtexQuoteInsertHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file.fileType != BibtexFileType) {
            return super.charTyped(char, project, editor, file)
        }

        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        if (char != '"' || document.textLength < offset + 1) {
            return super.charTyped(char, project, editor, file)
        }

        if (offset < 0 || offset + 1 >= document.textLength) {
            return super.charTyped(char, project, editor, file)
        }

        // Do not insert a quote when there is one right in front of the cursor
        if (document.getText(TextRange.from(offset, 1)) == "\"") {
            document.deleteString(offset, offset + 1)
            return Result.STOP
        }

        editor.document.insertString(editor.caretModel.offset, "\"")
        return super.charTyped(char, project, editor, file)
    }
}