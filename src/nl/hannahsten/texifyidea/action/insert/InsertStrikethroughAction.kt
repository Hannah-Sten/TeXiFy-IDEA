package nl.hannahsten.texifyidea.action.insert

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.action.InsertEditorAction
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.ULEM
import nl.hannahsten.texifyidea.util.insertUsepackage

/**
 * @author Hannah Schellekens
 */
class InsertStrikethroughAction : InsertEditorAction(
    "Strikethrough (ulem package)",
    "\\sout{",
    "}"
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        super.actionPerformed(file, project, textEditor)

        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return

        runWriteAction(project, file) {
            psiFile.insertUsepackage(ULEM)
        }
    }
}
