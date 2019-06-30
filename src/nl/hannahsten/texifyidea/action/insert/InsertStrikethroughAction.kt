package nl.hannahsten.texifyidea.action.insert

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.InsertEditorAction
import nl.hannahsten.texifyidea.util.PackageUtils

/**
 * @author Hannah Schellekens
 */
class InsertStrikethroughAction : InsertEditorAction(
        "Strikethrough (ulem package)",
        TexifyIcons.FONT_STRIKETHROUGH,
        "\\sout{", "}"
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        super.actionPerformed(file, project, textEditor)

        val document = textEditor.editor.document
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
        val packageName = "ulem"

        runWriteAction(project) {
            PackageUtils.insertUsepackage(document, psiFile, packageName, null)
        }
    }
}
