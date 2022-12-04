package nl.hannahsten.texifyidea.run.legacy.pdfviewer

import com.intellij.execution.process.ProcessHandler
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Provides forward search for the available pdf viewers.
 */
class ViewerForwardSearch(private val viewer: PdfViewer) {

    /**
     * Execute forward search when the process is done.
     *
     * In the case that no tex file is open, forward search from the first line of the main file that is selected in the
     * run config.
     */
    fun execute(handler: ProcessHandler, runConfig: LatexRunConfiguration, environment: ExecutionEnvironment, focusAllowed: Boolean = true) {
        // We have to find the file and line number before scheduling the forward search.
        val editor = environment.project.currentTextEditor()?.editor

        // Get the line number in the currently open file
        val line = editor?.document?.getLineNumber(editor.caretOffset())?.plus(1) ?: 0

        // Get the currently open file to use for forward search.
        val currentPsiFile = editor?.document?.psiFile(environment.project)
            // Get the main file from the run configuration as a fallback.
            ?: runConfig.options.mainFile.resolve()?.psiFile(environment.project)
            ?: return

        handler.addProcessListener(OpenViewerListener(viewer, runConfig, currentPsiFile.virtualFile.path, line, runConfig.project, focusAllowed))
    }
}