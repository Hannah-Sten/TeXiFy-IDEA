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
 * On every added (or deleted, see [AutoCompileBackspaceHandler]) character, make sure the document is compiled.
 */
class AutocompileTypedHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if(file.fileType == LatexFileType && TexifySettings.getInstance().isAutoCompileImmediate()) {
            // Only do this for latex files and if the option is enabled
            AutoCompileState.requestAutoCompilation(project)
        }
        return super.charTyped(char, project, editor, file)
    }
}

class AutoCompileBackspaceHandler : BackspaceHandlerDelegate() {

    override fun beforeCharDeleted(c: Char, file: PsiFile, editor: Editor) {
    }

    override fun charDeleted(c: Char, file: PsiFile, editor: Editor): Boolean {
        val project = editor.project
        if (file.isLatexFile() && project != null && TexifySettings.getInstance().isAutoCompileImmediate()) {
            AutoCompileState.requestAutoCompilation(project)
        }
        return false
    }
}