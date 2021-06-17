package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputType.STDERR
import com.intellij.execution.ui.CommonParameterFragments.setMonospaced
import com.intellij.ide.DataManager
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.dialog
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.macro.sortOutMacros
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationAbstractPathOption
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationPathOption
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.availablePdfViewers
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.toVector
import org.jetbrains.annotations.NotNull
import java.io.OutputStream
import javax.swing.DefaultComboBoxModel

class PdfViewerStep(
    override val provider: StepProvider, override var configuration: LatexRunConfiguration
) : Step, PersistentStateComponent<PdfViewerStep.State> {

    class State : BaseState() {

        @get:Attribute("pdfViewer", converter = PdfViewer.Converter::class)
        var pdfViewer by property(defaultPdfViewer) { it == defaultPdfViewer }

        @get:Attribute("pdfFilePath", converter = LatexRunConfigurationAbstractPathOption.Converter::class)
        var pdfFilePath: LatexRunConfigurationPathOption by property(LatexRunConfigurationPathOption()) { it.isDefault() }
    }

    private var state = State()

    fun getDefaultPdfFilePath() = configuration.outputFilePath

    companion object {

        val defaultPdfViewer = availablePdfViewers().firstOrNull()
    }

    override fun configure() {
        // We have to get the data context in the setter, any data component will do
        var comboBoxBuilder: CellBuilder<ComboBox<PdfViewer>>? = null
        val panel = panel {
            row("PDF viewer:") {
                comboBoxBuilder = comboBox(
                    DefaultComboBoxModel(availablePdfViewers().toVector()),
                    getter = { state.pdfViewer ?: defaultPdfViewer },
                    setter = { state.pdfViewer = it }
                ).focused()
            }

            row("PDF file:") {
                val textFieldBuilder = textFieldWithBrowseButton(
                    getter = { state.pdfFilePath.pathWithMacro ?: getDefaultPdfFilePath() },
                    setter = {
                        val (resolvedPath, pathWithMacro) = sortOutMacros(comboBoxBuilder?.component?.getComponent(0) ?: return@textFieldWithBrowseButton, configuration, it)
                        state.pdfFilePath = LatexRunConfigurationPathOption(resolvedPath, pathWithMacro)
                    },
                    fileChooserDescriptor = FileTypeDescriptor("PDF File", ".pdf", ".dvi")
                )
                MacrosDialog.addMacroSupport(
                    textFieldBuilder.component.textField as @NotNull ExtendableTextField,
                    MacrosDialog.Filters.DIRECTORY_PATH
                ) { false }
                setMonospaced(textFieldBuilder.component.textField)
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
                console.startStep(id, this@PdfViewerStep, this)
                runInEdt {
                    val exit = try {
                        openViewer(configuration.project.currentTextEditor()?.file)
                    }
                    catch (e: TeXception) {
                        this.notifyTextAvailable(e.message ?: "", STDERR)
                        -1
                    }
                    // Next step should wait for the pdf to open, as it may require the pdf
                    super.notifyProcessTerminated(exit)
                    console.finishStep(id, exit)
                }
            }
        }
    }

    private fun openViewer(texFile: VirtualFile?): Int {
        val project = configuration.project
        val currentEditor = configuration.project.currentTextEditor()
        val pdfViewer = state.pdfViewer

        // Forward search if the file currently open in the editor belongs to the file set of the main file that we are compiling.
        return if (texFile != null && texFile.psiFile(project) in ReferencedFileSetCache().fileSetFor(
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