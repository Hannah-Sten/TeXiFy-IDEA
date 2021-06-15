package nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.zathura

import com.intellij.execution.RunManager
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.ViewerConversation
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.selectedRunConfig

object ZathuraConversation : ViewerConversation() {

    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean): Int {
        val pdfPathGuess = pdfPath ?: guessPdfPath(project, sourceFilePath)

        if (pdfPathGuess != null) {
            val path = PathManager.getBinPath()
            val name = ApplicationNamesInfo.getInstance().scriptName
            val command =
                """zathura --synctex-forward="$line:1:$sourceFilePath" --synctex-editor-command="$path/$name.sh --line %{line} $sourceFilePath" $pdfPathGuess"""
            return Runtime.getRuntime().exec(arrayOf("bash", "-c", command)).exitValue()
        }
        else {
            throw TeXception("Could not execute forward search, please make sure you have compiled the document first.")
        }
    }

    /**
     * Guess the path of the pdf file to forward search to based on the currently selected run configuration (as this is
     * often the last configuration that is run).
     */
    private fun guessPdfPath(project: Project, sourceFilePath: String): String? {
        val sourceVirtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$sourceFilePath") ?: return null
        val sourcePsiFile = PsiManager.getInstance(project).findFile(sourceVirtualFile) ?: return null

        // First check if the file in the editor (sourceFilePath) is in the file set of the main file of the latest run run configuration.
        // If so, guess the main file (pdf) of that run config as the pdf.
        val runConfig = project.selectedRunConfig() ?: return null
        return if (runConfig.options.mainFile.resolve()?.psiFile(project)?.referencedFileSet()?.contains(sourcePsiFile) == true) {
            // outputFilePath contains the file name and pdf extension (already). We don't have to add it.
            runConfig.outputFilePath
        }
        // If not, search for a run configuration that compiles the root file of the current file,
        // and use the output path that is specified there.
        else {
            runConfigThatCompilesFile(sourceVirtualFile, project)?.outputFilePath ?: return null
        }
    }

    /**
     * Get the run config that compiles the virtualFile, i.e.,
     * the run configuration that has [virtualFile] as its main file.
     */
    private fun runConfigThatCompilesFile(virtualFile: VirtualFile, project: Project): LatexRunConfiguration? =
        (RunManagerImpl.getInstanceImpl(project) as RunManager)
            .allConfigurationsList
            .filterIsInstance<LatexRunConfiguration>()
            .firstOrNull { it.options.mainFile.resolve() == virtualFile }
}