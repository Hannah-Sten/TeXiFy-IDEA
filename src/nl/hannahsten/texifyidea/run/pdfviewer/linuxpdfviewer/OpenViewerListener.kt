package nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
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
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        // Do nothing.
    }

    override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
        // Do nothing.
    }

    override fun startNotified(event: ProcessEvent) {
        runAsync {
            try {
                when (viewer) {
                    is InternalPdfViewer -> viewer.conversation!!.forwardSearch(pdfPath = runConfig.outputFilePath, sourceFilePath = sourceFilePath, line = line, project = project, focusAllowed = focusAllowed)
                    is ExternalPdfViewer -> viewer.forwardSearch(pdfPath = runConfig.outputFilePath, sourceFilePath = sourceFilePath, line = line, project = project, focusAllowed = focusAllowed)
                    else -> {}
                }
                // Set this viewer as viewer to forward search to in the future.
                (ActionManager.getInstance().getAction("texify.ForwardSearch") as? ForwardSearchAction)?.viewer = viewer
            }
            catch (ignored: TeXception) {
            }
        }

        // Reset to default
        runConfig.allowFocusChange = true
    }
}