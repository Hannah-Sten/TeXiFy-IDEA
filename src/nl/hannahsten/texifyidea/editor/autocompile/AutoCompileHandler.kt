package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.isLatexFile

/**
 * On every added (or deleted, see [AutoCompileBackspacehandler]) character, make sure the document is compiled.
 */
class AutocompileHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {

        run {
            // Only do this for latex files and if the option is enabled
            if (file.fileType != LatexFileType || !TexifySettings.getInstance().autoCompile) {
                return@run
            }

            AutoCompileState.documentChanged(project)
        }

        return super.charTyped(char, project, editor, file)
    }
}

class AutoCompileBackspacehandler : BackspaceHandlerDelegate() {

    override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) {
        val project = editor.project
        if (file.isLatexFile() && project != null && TexifySettings.getInstance().autoCompile) {
            AutoCompileState.documentChanged(project)
        }
    }

    override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean {
        return false
    }
}