package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState

/**
 * Execute a forward search with the selected viewer after the compilation is done.
 */
class OpenViewerListener(
    private val viewer: PdfViewer,
    private val outputPath: String,
    private val session: LatexRunSessionState,
    private val sourceFilePath: String,
    val line: Int,
    val project: Project,
    val focusAllowed: Boolean = false
) :
    ProcessListener {

    override fun processTerminated(event: ProcessEvent) {
        if (event.exitCode == 0) {
            try {
                // ensure the viewer is open, especially for Sumatra
                viewer.openFile(outputPath, project, focusAllowed = focusAllowed)
                viewer.forwardSearch(
                    outputPath = outputPath,
                    sourceFilePath = sourceFilePath,
                    line = line,
                    project = project,
                    focusAllowed = focusAllowed,
                    session = session,
                )
                // Set this viewer as viewer to forward search to in the future.
                (ActionManager.getInstance().getAction("texify.ForwardSearch") as? ForwardSearchAction)?.viewer =
                    viewer
            }
            catch (ignored: TeXception) {
            }
        }
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
