package nl.hannahsten.texifyidea.run.linuxpdfviewer

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewers
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import org.jetbrains.concurrency.runAsync

/**
 * Execute a forward search with the selected viewer after the compilation is done.
 */
class OpenViewerListener(
        private val viewer: PdfViewer,
        val runConfig: LatexRunConfiguration,
        private val sourceFilePath: String,
        val line: Int,
        val project: Project,
        val focusAllowed: Boolean = true
) :
    ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0) {
            runAsync {
                try {
                    when(viewer) {
                        is InternalPdfViewer -> viewer.conversation!!.forwardSearch(pdfPath = runConfig.outputFilePath, sourceFilePath = sourceFilePath, line = line, project = project, focusAllowed = focusAllowed)
                        is ExternalPdfViewer -> viewer.forwardSearch(pdfPath = runConfig.outputFilePath, sourceFilePath = sourceFilePath, line = line, project = project, focusAllowed = focusAllowed)
                        else -> {}
                    }
                }
                catch (ignored: TeXception) {
                }
            }
        }

        // Reset to default
        runConfig.allowFocusChange = true
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        // Do nothing.
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
        // Do nothing.
    }

    override fun startNotified(event: ProcessEvent) {
        // Do nothing.
    }
}