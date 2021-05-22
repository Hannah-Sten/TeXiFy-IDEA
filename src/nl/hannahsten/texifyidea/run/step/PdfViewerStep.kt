package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache
import nl.hannahsten.texifyidea.util.files.psiFile
import java.io.OutputStream

class PdfViewerStep(override val provider: StepProvider, override val configuration: LatexRunConfiguration) : Step {

    val pdfViewer: PdfViewer = InternalPdfViewer.EVINCE

    override fun configure() {
        TODO("Not yet implemented")
    }

    override fun execute(id: String, console: LatexExecutionConsole): ProcessHandler {
        return object : ProcessHandler() {
            override fun destroyProcessImpl() = notifyProcessTerminated(0)

            override fun detachProcessImpl() = notifyProcessDetached()

            override fun detachIsDefault(): Boolean = false

            override fun getProcessInput(): OutputStream? = null

            override fun startNotify() {
                super.startNotify()
                runInEdt {
                    val currentEditor = configuration.project.currentTextEditor() ?: return@runInEdt
                    val currentFile = currentEditor.file ?: return@runInEdt
                    openViewer(currentFile)
                }
                // Immediately notify that we are done opening the pdf file, the next step shouldn't wait for the PDF file to open.
                super.notifyProcessTerminated(0)
            }
        }
    }

    private fun openViewer(texFile: VirtualFile) {
        // TODO check which PDF viewers have a separate open/forward search function.
        //  see OpenViewerListener, SumatraListener, OpenCustomPDFViewerListener
        val project = configuration.project
        val currentEditor = configuration.project.currentTextEditor()

        // Forward search if the file currently open in the editor belongs to the fileset of the main file that we are compiling.
        if (texFile.psiFile(project) in ReferencedFileSetCache().fileSetFor(configuration.psiFile!!) && currentEditor != null) {
            ForwardSearchAction(pdfViewer).actionPerformed(texFile, project, currentEditor)
        }
        // TODO else just open the pdf without forward search, do we want this? Should be possible, right?
    }
}