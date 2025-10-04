package nl.hannahsten.texifyidea.action

import com.intellij.execution.RunManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.step.PdfViewerStep
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.latexTemplateRunConfig
import nl.hannahsten.texifyidea.util.selectedRunConfig

open class ForwardSearchAction(var viewer: PdfViewer? = null) : EditorAction(
    name = "_Forward Search"
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (file.fileType !is LatexFileType) return
        val viewer = this.viewer ?: return
        if (!viewer.isAvailable() || !viewer.isForwardSearchSupported) return

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1
        try {
            viewer.forwardSearch(null, file.path, line, project, focusAllowed = true)
        }
        catch (e: TeXception) {
            // Show a notification if the forward search fails, but only catch TeXception and let other unexpected exceptions bubble up.
            Notification(
                "LaTeX", "Forward search error", "${e.message}",
                NotificationType.WARNING
            ).notify(project)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = (
            e.project?.selectedRunConfig()?.compileSteps?.filterIsInstance<PdfViewerStep>()?.firstOrNull()?.state?.pdfViewer == viewer
                || (e.project?.selectedRunConfig() == null && e.project?.latexTemplateRunConfig()?.compileSteps?.filterIsInstance<PdfViewerStep>()?.firstOrNull()?.state?.pdfViewer == viewer)
            ) && e.getData(CommonDataKeys.VIRTUAL_FILE)?.fileType is LatexFileType
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    /**
     * Guess the path of the pdf to perform forward search to.
     *
     * Looks up the file set for [file] and gets all run configurations that compile a file from this file set.
     * Then guess which of these run configurations actually compile [file], and return the pdf path of this
     * configuration.
     */
    private fun guessPdfFile(file: VirtualFile, project: Project): String? {
        val psiFile = file.psiFile(project) ?: return null
        val fileSet = ReferencedFileSetCache().fileSetFor(psiFile)

        val mainFileCandidates = RunManager.getInstance(project)
            .getConfigurationsList(latexRunConfigurationType())
            .asSequence()
            .mapNotNull { it as LatexRunConfiguration }
            .filter { it.options.mainFile.resolve()?.psiFile(project) in fileSet }

        return mainFileCandidates.sortedBy { it.options.lastRun }.firstOrNull()?.outputFilePath
    }

    /**
     * @return Exit code.
     */
    fun forwardSearch(
        texFile: VirtualFile,
        project: Project,
        absolutePdfPath: String? = guessPdfFile(texFile, project),
        textEditor: TextEditor?
    ): Int {
        if (viewer == null) throw TeXception("No pdf viewer found")
        if (absolutePdfPath == null) throw TeXception("Pdf file for ${texFile.path} was not found.")

        if (!viewer!!.isAvailable()) {
            throw TeXception("The pdf viewer $viewer is not available.")
        }

        val line = textEditor?.editor?.document?.getLineNumber(textEditor.editor.caretModel.offset)?.plus(1) ?: 1

        val exitCode = when (viewer) {
            is ExternalPdfViewer -> {
                (viewer as ExternalPdfViewer).forwardSearch(absolutePdfPath, texFile.path, line, project, focusAllowed = true)
                0
            }

            // todo pdf viewers https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/4067/files#diff-cea588f28b406e720fd6cc160b66c77366f3369efd300158c28842a955f66c7a
//            is SystemDefaultViewer, is CustomPdfViewer -> {
//                val executable = if (viewer is SystemDefaultViewer) {
//                    (viewer as SystemDefaultViewer).executableName
//                }
//                else {
//                    (viewer as CustomPdfViewer).executablePath
//                }
//                // todo working dir, arguments, env vars
//                // Keep process running after timeout, for example the Evince command will not exit
//                // when a pdf is opened, regardless of whether there is an error or not
//                // but we might need to continue with other steps.
//                runCommandWithExitCode(executable, absolutePdfPath, timeout = 1L, killAfterTimeout = false).second
//            }

//            is InternalPdfViewer -> (viewer as InternalPdfViewer).forwardSearch(
//                absolutePdfPath,
//                texFile.path,
//                line,
//                project,
//                focusAllowed = true
//            ) ?: throw TeXception("There was a problem communicating with pdf viewer $viewer")

            else -> throw TeXception("Running pdf viewer $viewer is not yet implemented.")
        }

        if (exitCode == 0) {
            // Set this viewer as viewer to forward search to in the future.
            (ActionManager.getInstance().getAction("texify.ForwardSearch") as? ForwardSearchAction)?.viewer = viewer
        }

        return exitCode
    }

}
