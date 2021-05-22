package nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.runInEdt
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.files.openedEditor
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Provides forward search for the available pdf viewers.
 */
class ViewerForwardSearch(private val viewer: PdfViewer) {

    /**
     * Execute forward search when the process is done.
     */
    fun execute(runConfig: LatexRunConfiguration, focusAllowed: Boolean = true) {
        // We have to find the file and line number before scheduling the forward search
        val mainPsiFile = runConfig.mainFile?.psiFile(runConfig.project) ?: return
        val editor = mainPsiFile.openedEditor() ?: return

        // Get the line number in the currently open file
        val line = editor.document.getLineNumber(editor.caretOffset()) + 1

        // Get the currently open file to use for forward search.
        val currentPsiFile = editor.document.psiFile(runConfig.project) ?: return

        // Set the OpenViewerListener to execute when the compilation is done.
        val sourceFilePath = currentPsiFile.virtualFile.path
        val project = runConfig.project

        when (viewer) {
            is InternalPdfViewer -> viewer.conversation!!.forwardSearch(pdfPath = runConfig.outputFilePath, sourceFilePath = sourceFilePath, line = line, project = project, focusAllowed = focusAllowed)
            is ExternalPdfViewer -> viewer.forwardSearch(pdfPath = runConfig.outputFilePath, sourceFilePath = sourceFilePath, line = line, project = project, focusAllowed = focusAllowed)
            else -> {}
        }
        // Set this viewer as viewer to forward search to in the future.
        (ActionManager.getInstance().getAction("texify.ForwardSearch") as? ForwardSearchAction)?.viewer = viewer
    }
//        OpenViewerListener(viewer, runConfig, currentPsiFile.virtualFile.path, line, runConfig.project, focusAllowed)
}