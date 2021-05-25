package nl.hannahsten.texifyidea.run.legacy.pdfviewer

import com.intellij.execution.process.ProcessHandler
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
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
    fun execute(handler: ProcessHandler, runConfig: LatexRunConfiguration, focusAllowed: Boolean = true) {
        // We have to find the file and line number before scheduling the forward search
        val mainPsiFile = runConfig.options.mainFile.resolve()?.psiFile(runConfig.project) ?: return
        val editor = mainPsiFile.openedEditor() ?: return

        // Get the line number in the currently open file
        val line = editor.document.getLineNumber(editor.caretOffset()) + 1

        // Get the currently open file to use for forward search.
        val currentPsiFile = editor.document.psiFile(runConfig.project) ?: return

        handler.addProcessListener(OpenViewerListener(viewer, runConfig, currentPsiFile.virtualFile.path, line, runConfig.project, focusAllowed))

    }
}