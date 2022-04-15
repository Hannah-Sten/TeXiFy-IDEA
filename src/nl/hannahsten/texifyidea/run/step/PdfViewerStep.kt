package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputType.STDERR
import com.intellij.execution.ui.CommonParameterFragments.setMonospaced
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.ui.components.dialog
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.macro.OutputDirMacro
import nl.hannahsten.texifyidea.run.macro.sortOutMacros
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationAbstractPathOption
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationPathOption
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.SupportedPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.availablePdfViewers
import nl.hannahsten.texifyidea.run.ui.compiler.ExecutableEditor
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetCache
import nl.hannahsten.texifyidea.util.files.isRoot
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.name
import nl.hannahsten.texifyidea.util.parentsOfType
import java.awt.event.MouseEvent
import java.io.File
import java.io.OutputStream
import java.util.*

class PdfViewerStep internal constructor(
    override val provider: StepProvider, override var configuration: LatexRunConfiguration
) : Step, PersistentStateComponent<PdfViewerStep.State> {

    override val name = "PDF Viewer step"

    class State : BaseState() {

        @get:Attribute("pdfViewer", converter = PdfViewer.Converter::class)
        var pdfViewer: PdfViewer? by property(defaultPdfViewer) { it == defaultPdfViewer }

        @get:Attribute("pdfFilePath", converter = LatexRunConfigurationAbstractPathOption.Converter::class)
        var pdfFilePath: LatexRunConfigurationPathOption by property(LatexRunConfigurationPathOption()) { it.isDefault() }

        @get:Attribute
        var viewerArguments by string()

        @get:Attribute
        var envs by map<String, String>()

        @get:Attribute
        var isPassParentEnvs by property(EnvironmentVariablesData.DEFAULT.isPassParentEnvs)
    }

    private var state = State()

    /**
     * The default path is given by a macro resolving to the output directory plus the main file name and extension.
     */
    fun getDefaultPdfFilePathWithMacro(): LatexRunConfigurationPathOption {
        val mainFile = configuration.options.mainFile.resolve()
        val outputFormat = if (configuration.options.outputFormat == LatexCompiler.OutputFormat.DEFAULT) {
            "pdf"
        }
        else {
            configuration.options.outputFormat.toString()
        }
        val pdfFileName = mainFile?.nameWithoutExtension + "." + outputFormat.lowercase(Locale.getDefault())
        val pathWithMacro = File(OutputDirMacro().macro, pdfFileName).path
        val resolvedPath = File(configuration.options.outputPath.resolvedPath, pdfFileName).path
        return LatexRunConfigurationPathOption(resolvedPath, pathWithMacro)
    }

    companion object {
        val defaultPdfViewer = availablePdfViewers().firstOrNull()
    }

    override fun isValid(): Boolean {
        return state.pdfViewer != null
    }

    override fun configure(context: DataContext) {
        val viewerEditor = ExecutableEditor<SupportedPdfViewer, PdfViewer>("PDF Viewer", availablePdfViewers()) { CustomPdfViewer(it) }
        setDefaultLayout(viewerEditor, state.pdfViewer)

        // todo whether this makes sense depends on the pdf viewer
        val viewerArguments = createParametersTextField("Pdf Viewer", state.viewerArguments)

        val environmentVariables = EnvironmentVariablesComponent().apply {
            label.isVisible = false
            envs = state.envs
            isPassParentEnvs = state.isPassParentEnvs
        }

        val panel = panel {
            row("PDF viewer:") {
                cell {
                    component(viewerEditor)
                    viewerArguments(CCFlags.growX, CCFlags.pushX)
                }
            }

            row("PDF file:") {
                val textFieldBuilder = textFieldWithBrowseButton(
                    getter = { state.pdfFilePath.pathWithMacro ?: getDefaultPdfFilePathWithMacro().resolvedPath!! },
                    setter = {
                        // We have to get the data context in the setter, any data component will do
                        val anyDataComponent = viewerEditor.component
                        val (resolvedPath, pathWithMacro) = sortOutMacros(anyDataComponent?.getComponent(0) ?: return@textFieldWithBrowseButton, configuration, it)
                        state.pdfFilePath = LatexRunConfigurationPathOption(resolvedPath, pathWithMacro)
                    },
                    fileChooserDescriptor = FileTypeDescriptor("PDF File", ".pdf", ".dvi")
                )
                MacrosDialog.addMacroSupport(
                    textFieldBuilder.component.textField as ExtendableTextField,
                    MacrosDialog.Filters.DIRECTORY_PATH
                ) { false }
                setMonospaced(textFieldBuilder.component.textField)
            }
            row("Environment variables:") {
                component(environmentVariables)
                    .comment("For this step only. Separate variables with semicolon: VAR=value; VAR1=value1")
            }
        }

        val modified = dialog(
            "Configure PDF Viewer Step",
            panel = panel,
            resizable = true,
        ).showAndGet()

        if (modified) {
            state.pdfViewer = viewerEditor.getSelectedExecutable()
        }
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

                runReadAction {

                    // This has both .psiFile calls which need to run in read action or EDT, and long-running operations which cannot run in EDT, so it has to be a read action.
                    val forwardSearch = try {
                        openViewer()
                    }
                    catch (e: TeXception) {
                        { throw e }
                    }

                    runInEdt {
                        val exit =
                            try {
                                // The forward search itselfs needs to run in EDT because of a synchronous refresh
                                forwardSearch()
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
    }

    private fun openViewer(): () -> Int {
        val project = configuration.project
        val currentEditor = configuration.project.currentTextEditor()
        val pdfViewer = state.pdfViewer
        val texFile = configuration.project.currentTextEditor()?.file
        val pdfFile = state.pdfFilePath.resolvedPath ?: throw TeXception("pdf not specified")

        // Needs to run on EDT
        val psiFile = texFile?.psiFile(project)

        val mainFile = configuration.options
            .mainFile
            .resolve()
            ?.psiFile(configuration.project)!!

        // Shouldn't run on EDT because it is slow
        val fileSet = ReferencedFileSetCache().fileSetFor(mainFile)

        val belongsToFileset = psiFile in fileSet

        // Do not forward search if we are editing the preamble
        val isEditingPreamble = true == psiFile?.run {
            // Only applicable for root files (or files included in the preamble, but we don't check that yet)
            if (!isRoot()) return@run false
            val offset = currentEditor?.editor?.caretOffset() ?: return@run false
            // Check if cursor is not in document environment
            findElementAt(offset)?.parentsOfType<LatexEnvironment>()?.any { it.name()?.text == "document" } == false
        }

        // Forward search if the file currently open in the editor belongs to the file set of the main file that we are compiling.
        return if (texFile != null && belongsToFileset && currentEditor != null && !isEditingPreamble) {
            { ForwardSearchAction(pdfViewer).forwardSearch(texFile, project, pdfFile, currentEditor) }
        }
        // If the file does not belong to the compiled file set, forward search to the first line of the main file.
        else {
            { ForwardSearchAction(pdfViewer).forwardSearch(configuration.options.mainFile.resolve()!!, project, pdfFile, null) }
        }
    }

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }

    override fun clone(): Step {
        // See RunConfigurationBase.clone()
        val newStep = PdfViewerStep(provider, configuration)
        newStep.loadState(State())
        newStep.state.copyFrom(this.state)
        return newStep
    }
}