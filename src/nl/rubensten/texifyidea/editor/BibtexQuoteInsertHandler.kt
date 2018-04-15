package nl.rubensten.texifyidea.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.file.BibtexFileType
import nl.rubensten.texifyidea.util.toTextRange

/**
 * @author Ruben Schellekens
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

        if (document.getText((offset..offset + 1).toTextRange()) == "\"") {
            document.deleteString(offset, offset + 1)
            return Result.STOP
        }

        editor.document.insertString(editor.caretModel.offset, "\"")
        return super.charTyped(char, project, editor, file)
    }
}