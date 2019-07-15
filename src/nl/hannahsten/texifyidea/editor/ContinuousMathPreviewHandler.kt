package nl.hannahsten.texifyidea.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.TexifySettings

class ContinuousMathPreviewHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {

        // Only do this for latex files and if the option is enabled
        if (file.fileType != LatexFileType || !TexifySettings.getInstance().continuousMathPreview) {
            return super.charTyped(char, project, editor, file)
        }

        return super.charTyped(char, project, editor, file)
    }

}