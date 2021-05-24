package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.SumatraConversation
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
                    openViewer(configuration.project.currentTextEditor()?.file)
                }
                // Immediately notify that we are done opening the pdf file, the next step shouldn't wait for the PDF file to open.
                super.notifyProcessTerminated(0)
            }
        }
    }

    private fun openViewer(texFile: VirtualFile?) {
        val project = configuration.project
        val currentEditor = configuration.project.currentTextEditor()

        // Sumatra is the only viewer that has a separate function for opening a file in the viewer.
        if (pdfViewer == InternalPdfViewer.SUMATRA) {
            SumatraConversation.openFile(configuration.outputFilePath)
        }

        // Forward search if the file currently open in the editor belongs to the file set of the main file that we are compiling.
        if (texFile != null && texFile.psiFile(project) in ReferencedFileSetCache().fileSetFor(configuration.mainFile?.psiFile(project)!!) && currentEditor != null) {
            ForwardSearchAction(pdfViewer).forwardSearch(texFile, project, currentEditor)
        }
        // If the file does not belong to the compiled file set, forward search to the first line of the main file.
        else {
            ForwardSearchAction(pdfViewer).forwardSearch(configuration.mainFile!!, project, null)
        }
    }
}