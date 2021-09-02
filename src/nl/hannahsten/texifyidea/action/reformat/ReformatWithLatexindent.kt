package nl.hannahsten.texifyidea.action.reformat

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.util.files.isLatexFile

/**
 * Run external tool 'latexindent.pl' to reformat the file.
 * This action is placed next to the standard Reformat action in the Code menu.
 *
 * @author Thomas
 */
class ReformatWithLatexindent : ExternalReformatAction("Reformat File with Latexindent", { it.isLatexFile() } ) {

    override fun getCommand(file: PsiFile): List<String> {
        // Ensure the document is saved, before we run anything on it
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
        if (document != null) {
            FileDocumentManager.getInstance().saveDocument(document)
        }
        return listOf("latexindent.pl", file.name)
    }

    override fun processOutput(output: String, file: PsiFile, project: Project) {
        replaceLatexFileContent(output, file, project)
    }
}