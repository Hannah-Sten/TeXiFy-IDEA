package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.action.preview.ShowEquationPreview
import nl.hannahsten.texifyidea.action.preview.ShowTikzPreview
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.parser.findOuterMathEnvironment

/**
 * On every added (or deleted, see [ContinuousPreviewBackspacehandler]) character, initiate a preview.
 */
class ContinuousPreviewHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {
        run {
            // Only do this for latex files and if the option is enabled
            if (file.fileType != LatexFileType || !TexifySettings.getState().continuousPreview) {
                return@run
            }

            val element = file.findElementAt(editor.caretModel.offset) ?: return@run

            val textEditor = if (editor is TextEditor) {
                editor
            }
            else {
                ShowEquationPreview().getTextEditor(project, null) ?: return@run
            }

            // Show corresponding preview depending on environment
            if (element.findOuterMathEnvironment() != null) {
                ShowEquationPreview().actionPerformed(file.virtualFile, project, textEditor)
            }
            else if (ShowTikzPreview().findTikzEnvironment(element) != null) {
                ShowTikzPreview().actionPerformed(file.virtualFile, project, textEditor)
            }
        }

        return super.charTyped(char, project, editor, file)
    }
}

class ContinuousPreviewBackspacehandler : BackspaceHandlerDelegate() {

    override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) {}

    override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean {
        return if (file.isLatexFile()) {
            ContinuousPreviewHandler().charTyped(c, file.project, editor, file)
            true
        }
        else {
            // Returning true would block functionality of other plugins (e.g. removing second brace of pair in [<cursor>])
            false
        }
    }
}