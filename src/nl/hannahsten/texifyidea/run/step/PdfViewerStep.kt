package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.serialization.defaultReadConfiguration
import com.intellij.ui.components.dialog
import com.intellij.ui.layout.panel
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.LatexRunConfigurationDirectoryOption
import nl.hannahsten.texifyidea.run.LatexRunConfigurationOptions
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.availablePdfViewers
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.SumatraConversation
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.toVector
import java.io.OutputStream
import javax.swing.DefaultComboBoxModel

class PdfViewerStep(
    override val provider: StepProvider, override val configuration: LatexRunConfiguration
) : Step, PersistentStateComponent<PdfViewerStep.State> {

    inner class State : BaseState() {

        @get:Attribute("pdfViewer", converter = PdfViewer.Converter::class)
        var pdfViewer by property(defaultPdfViewer) { it == defaultPdfViewer }

        @get:Attribute("pdfFilePath", converter = LatexRunConfigurationDirectoryOption.Converter::class)
        var pdfFilePath by property(LatexRunConfigurationDirectoryOption()) { it.resolvedPath == defaultPdfFilePath }
    }

    private var state = State()
    val defaultPdfViewer = availablePdfViewers().firstOrNull()
    val defaultPdfFilePath = configuration.outputFilePath


    override fun configure() {
        val panel = panel {
            row("PDF viewer:") {
                comboBox(
                    DefaultComboBoxModel(availablePdfViewers().toVector()),
                    getter = { state.pdfViewer ?: defaultPdfViewer },
                    setter = { state.pdfViewer = it }
                ).focused()
            }

            row("PDF file:") {
                textFieldWithBrowseButton(
                    getter = { state.pdfFilePath.resolvedPath ?: defaultPdfFilePath },
                    setter = { state.pdfFilePath.setPath(it) }
                )
            }
        }

        dialog(
            "Configure PDF Viewer Step",
            panel = panel,
            resizable = true,
        ).showAndGet()
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
        val pdfViewer = state.pdfViewer

        // Sumatra is the only viewer that has a separate function for opening a file in the viewer.
        if (pdfViewer == InternalPdfViewer.SUMATRA) {
            SumatraConversation.openFile(configuration.outputFilePath)
        }

        // Forward search if the file currently open in the editor belongs to the file set of the main file that we are compiling.
        if (texFile != null && texFile.psiFile(project) in ReferencedFileSetCache().fileSetFor(
                configuration.options.mainFile.resolve()?.psiFile(
                    project
                )!!
            ) && currentEditor != null
        ) {
            ForwardSearchAction(pdfViewer).forwardSearch(texFile, project, currentEditor)
        }
        // If the file does not belong to the compiled file set, forward search to the first line of the main file.
        else {
            ForwardSearchAction(pdfViewer).forwardSearch(configuration.options.mainFile.resolve()!!, project, null)
        }
    }

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }
}