package nl.hannahsten.texifyidea.action.reformat

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.isLatexFile

/**
 * Run external tool 'latexindent.pl' to reformat the file.
 * This action is placed next to the standard Reformat action in the Code menu.
 *
 * @author Thomas
 */
class ReformatWithLatexindent : ExternalReformatAction({ it.isLatexFile() }) {

    override fun getCommand(file: PsiFile): List<String> {
        // Ensure the document is saved, before we run anything on it
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
        if (document != null) {
            FileDocumentManager.getInstance().saveDocument(document)
        }
        // latexindent as command seems to work more often than latexindent.pl
        return listOf("latexindent") + ParametersListUtil.parse(TexifySettings.getInstance().latexIndentOptions) + listOf(file.name)
    }

    override fun processOutput(output: String, file: PsiFile, project: Project) {
        replaceLatexFileContent(output, file, project)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}