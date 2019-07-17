package nl.hannahsten.texifyidea.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.action.preview.ShowEquationPreview
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.openedEditor
import java.util.*

class ContinuousMathPreviewHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {

        // Only do this for latex files and if the option is enabled
        // todo only in math
        if (file.fileType != LatexFileType || !TexifySettings.getInstance().continuousMathPreview) {
            return super.charTyped(char, project, editor, file)
        }

        val textEditor = getTextEditor(project, editor) ?: return super.charTyped(char, project, editor, file)
        ShowEquationPreview().actionPerformed(file.virtualFile, project, textEditor)

        return super.charTyped(char, project, editor, file)
    }

    private fun getTextEditor(project: Project, fileEditor: Editor?): TextEditor? {
        if (fileEditor is TextEditor) {
            return fileEditor
        }

        val editors = FileEditorManager.getInstance(project).selectedEditors
        return Arrays.stream(editors)
                .filter { e -> e is TextEditor }
                .map { e -> e as TextEditor }
                .findFirst()
                .orElse(null)
    }

}