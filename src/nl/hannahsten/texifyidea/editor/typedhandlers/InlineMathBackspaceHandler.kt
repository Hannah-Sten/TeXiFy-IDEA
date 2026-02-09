package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase.getElementAtCaret
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.get
import nl.hannahsten.texifyidea.util.orFalse
import nl.hannahsten.texifyidea.util.parser.inVerbatim

/**
 * In the situation $<caret>$, pressing backspace should delete both $.
 * See [LatexTypedHandler] for how the second $ was inserted.
 *
 * @author Thomas
 */
class InlineMathBackspaceHandler : BackspaceHandlerDelegate() {

    override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) {
        if (c == '$' && file.isLatexFile() && !getElementAtCaret(editor)?.inVerbatim().orFalse()) {
            val offset = editor.caretOffset()
            if (file.document()?.get(offset) == "$") {
                file.document()?.deleteString(offset, offset + 1)
            }
        }
    }

    override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean = false
}