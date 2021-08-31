package nl.hannahsten.texifyidea.action.reformat

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.runWriteCommandAction

/**
 * Run external tool 'latexindent.pl' to reformat the file.
 * This action is placed next to the standard Reformat action in the Code menu.
 *
 * @author Thomas
 */
class ReformatWithLatexindent : ExternalReformatAction("Reformat File with Latexindent", { it.isLatexFile() } ) {

    override fun getCommand(fileName: String): List<String> {
        return listOf("latexindent.pl", fileName)
    }

    override fun processOutput(output: String, file: PsiFile, project: Project) {
        // Assumes first child is LatexContent
        val newFile = LatexPsiHelper(project).createFromText(output)
        runWriteCommandAction(project) {
            file.node.replaceChild(file.node.firstChildNode, newFile.node.firstChildNode)
        }
    }
}