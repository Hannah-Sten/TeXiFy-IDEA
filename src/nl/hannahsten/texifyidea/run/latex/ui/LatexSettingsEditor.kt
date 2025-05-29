package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.ui.EditorTextField
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.PDFLATEX
import nl.hannahsten.texifyidea.run.latex.LatexCommandLineOptionsCache
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexOutputPath
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import java.awt.event.ItemEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author Sten Wessel
 */
class LatexSettingsEditor(private var project: Project) : SettingsEditor<LatexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var compiler: LabeledComponent<ComboBox<LatexCompiler>>
    private lateinit var enableCompilerPath: JBCheckBox
    private lateinit var compilerPath: TextFieldWithBrowseButton
    private lateinit var compilerArguments: LabeledComponent<EditorTextField>
    private lateinit var environmentVariables: EnvironmentVariablesComponent
    private lateinit var beforeRunCommand: LabeledComponent<RawCommandLineEditor>
    private lateinit var mainFile: LabeledComponent<ComponentWithBrowseButton<*>>
    private lateinit var outputPath: LabeledComponent<ComponentWithBrowseButton<*>>

    // Not shown on non-MiKTeX systems
    private var auxilPath: LabeledComponent<ComponentWithBrowseButton<*>>? = null

    private var expandMacrosEnvVariables: JBCheckBox? = null
    private var compileTwice: JBCheckBox? = null
    private lateinit var outputFormat: LabeledComponent<ComboBox<Format>>
    private lateinit var latexDistribution: LabeledComponent<ComboBox<LatexDistributionType>>
    private val extensionSeparator = TitledSeparator("Extensions")
    private lateinit var externalToolsPanel: RunConfigurationPanel

    private lateinit var pdfViewer: LabeledComponent<ComboBox<PdfViewer?>>

    /** Whether to require focus after compilation. */
    private lateinit var requireFocus: JBCheckBox

    /** Whether to enable the custom pdf viewer command text field. */
    private lateinit var enableViewerCommand: JBCheckBox

    /** Allow users to specify a custom pdf viewer command. */
    private lateinit var viewerCommand: JBTextField

    // Discard all non-confirmed user changes made via the UI
    override fun resetEditorFrom(runConfiguration: LatexRunConfiguration) {
        // Reset the selected compiler.
        compiler.component.selectedItem = runConfiguration.compiler

        // Reset the custom compiler path
        compilerPath.text = runConfiguration.compilerPath ?: ""
        enableCompilerPath.isSelected = runConfiguration.compilerPath != null

        pdfViewer.component.selectedItem = runConfiguration.pdfViewer
        requireFocus.isSelected = runConfiguration.requireFocus
        requireFocus.isVisible = runConfiguration.pdfViewer?.let {
            it.isForwardSearchSupported && it.isFocusSupported
        } ?: false

        // Reset the pdf viewer command
        viewerCommand.text = runConfiguration.viewerCommand ?: ""
        enableViewerCommand.isSelected = runConfiguration.viewerCommand != null

        // Reset compiler arguments
        val args = runConfiguration.compilerArguments
        compilerArguments.component.text = args ?: ""

        // Reset environment variables
        environmentVariables.envData = runConfiguration.environmentVariables
        if (expandMacrosEnvVariables != null) {
            expandMacrosEnvVariables!!.isSelected = runConfiguration.expandMacrosEnvVariables
        }

        beforeRunCommand.component.text = runConfiguration.beforeRunCommand

        // Reset the main file to compile.
        val txtFile = mainFile.component as TextFieldWithBrowseButton
        txtFile.text = runConfiguration.mainFile?.path ?: ""

        if (auxilPath != null) {
            val auxilPathTextField = auxilPath!!.component as TextFieldWithBrowseButton
            auxilPathTextField.text = runConfiguration.auxilPath.virtualFile?.path ?: runConfiguration.auxilPath.pathString
        }

        val outputPathTextField = outputPath.component as TextFieldWithBrowseButton
        // We may be editing a run configuration template, don't resolve any path
        outputPathTextField.text = runConfiguration.outputPath.virtualFile?.path ?: runConfiguration.outputPath.pathString

        // Reset whether to compile twice
        if (compileTwice != null) {
            if (runConfiguration.compiler?.handlesNumberOfCompiles == true) {
                compileTwice!!.isVisible = false
                runConfiguration.compileTwice = false
            }
            else {
                compileTwice!!.isVisible = true
            }
            compileTwice!!.isSelected = runConfiguration.compileTwice

            // If we need to compile twice, make sure the LatexCommandLineState knows
            if (runConfiguration.compileTwice) {
                runConfiguration.isLastRunConfig = false
            }
        }

        // Reset output format.
        // Make sure to use the output formats relevant for the chosen compiler
        if (runConfiguration.compiler != null) {
            outputFormat.component.removeAllItems()
            for (item in runConfiguration.compiler!!.outputFormats) {
                outputFormat.component.addItem(item)
            }
            if (runConfiguration.compiler!!.outputFormats.contains(runConfiguration.outputFormat)) {
                outputFormat.component.selectedItem = runConfiguration.outputFormat
            }
            else {
                outputFormat.component.selectedItem = Format.PDF
            }
        }

        // Reset LaTeX distribution
        latexDistribution.component.selectedItem = runConfiguration.latexDistribution

        // Reset project.
        project = runConfiguration.project

        // Reset run configs
        externalToolsPanel.configurations = runConfiguration.getAllAuxiliaryRunConfigs().toMutableSet()
    }

    // Confirm the changes, i.e. copy current UI state into the target settings object.
    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: LatexRunConfiguration) {
        // Apply chosen compiler.
        val chosenCompiler = compiler.component.selectedItem as? LatexCompiler ?: PDFLATEX
        runConfiguration.compiler = chosenCompiler

        // Remove bibtex run config when switching to a compiler which includes running bibtex
        val includesBibtex = runConfiguration.compiler?.includesBibtex == true
        val includesMakeindex = runConfiguration.compiler?.includesMakeindex == true
        if (includesBibtex || includesMakeindex) {
            if (includesBibtex) {
                runConfiguration.bibRunConfigs = setOf()
            }
            if (includesMakeindex) {
                runConfiguration.makeindexRunConfigs = setOf()
            }
            // Panel remains visible, to allow adding ExternalToolRunConfiguration
        }
        else {
            // Update run config based on UI
            runConfiguration.bibRunConfigs = externalToolsPanel.configurations.filter { it.type is BibtexRunConfigurationType }.toSet()
            runConfiguration.makeindexRunConfigs = externalToolsPanel.configurations.filter { it.type is MakeindexRunConfigurationType }.toSet()
            runConfiguration.externalToolRunConfigs = externalToolsPanel.configurations.filter { it.type is ExternalToolRunConfigurationType }.toSet()
        }

        // Apply custom compiler path if applicable
        runConfiguration.compilerPath = if (enableCompilerPath.isSelected) compilerPath.text else null

        runConfiguration.pdfViewer = pdfViewer.component.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer
        runConfiguration.requireFocus = requireFocus.isSelected

        // Apply custom pdf viewer command
        runConfiguration.viewerCommand = if (enableViewerCommand.isSelected) viewerCommand.text else null

        // Apply custom compiler arguments
        runConfiguration.compilerArguments = compilerArguments.component.text

        // Apply environment variables
        runConfiguration.environmentVariables = environmentVariables.envData

        // Apply parse macros in environment variables
        runConfiguration.expandMacrosEnvVariables = expandMacrosEnvVariables?.isSelected ?: false

        runConfiguration.beforeRunCommand = beforeRunCommand.component.text

        // Apply main file.
        val txtFile = mainFile.component as TextFieldWithBrowseButton
        val filePath = txtFile.text

        runConfiguration.setMainFile(filePath)

        val outputPathTextField = outputPath.component as TextFieldWithBrowseButton
        if (!outputPathTextField.text.endsWith("/bin")) {
            runConfiguration.setFileOutputPath(outputPathTextField.text)
        }

        if (auxilPath != null) {
            val auxilPathTextField = auxilPath!!.component as TextFieldWithBrowseButton
            runConfiguration.setFileAuxilPath(auxilPathTextField.text)
        }

        if (compileTwice != null) {
            // Only show option to configure number of compiles when applicable
            if (runConfiguration.compiler?.handlesNumberOfCompiles == true) {
                compileTwice!!.isVisible = false
                runConfiguration.compileTwice = false
            }
            else {
                compileTwice!!.isVisible = true
                runConfiguration.compileTwice = compileTwice!!.isSelected
            }

            // If we need to compile twice, make sure the LatexCommandLineState knows
            if (runConfiguration.compileTwice) {
                runConfiguration.isLastRunConfig = false
            }
        }

        // Apply output format.
        val format = outputFormat.component.selectedItem as Format?
        runConfiguration.outputFormat = format ?: Format.PDF

        // Apply LaTeX distribution
        runConfiguration.latexDistribution = latexDistribution.component.selectedItem as LatexDistributionType? ?: LatexDistributionType.TEXLIVE

        if (chosenCompiler == LatexCompiler.ARARA) {
            outputPath.isVisible = false
            auxilPath?.isVisible = false
            outputFormat.isVisible = false
        }
        else {
            outputPath.isVisible = true
            auxilPath?.isVisible = true
            outputFormat.isVisible = true
        }
    }

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    private fun createUIComponents() {
        // Layout
        panel = JPanel()
        panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

        addCompilerPathField(panel)

        addPdfViewerCommandField(panel)

        // Optional custom compiler arguments
        val argumentsLabel = JLabel("Custom compiler arguments")
        val argumentsEditor = EditorTextField("", project, PlainTextFileType.INSTANCE)
        argumentsLabel.labelFor = argumentsEditor
        val selectedCompiler = compiler.component.selectedItem as LatexCompiler
        project.let { project ->
            val options = LatexCommandLineOptionsCache.getOptionsOrFillCache(selectedCompiler.executableName, project)
            LatexArgumentsCompletionProvider(options).apply(argumentsEditor)
        }

        compilerArguments = LabeledComponent.create(argumentsEditor, "Custom compiler arguments")
        panel.add(compilerArguments)

        environmentVariables = EnvironmentVariablesComponent()
        panel.add(environmentVariables)

        expandMacrosEnvVariables = JBCheckBox("Expand macros in environment variables")
        expandMacrosEnvVariables!!.isSelected = false

        val environmentVariableTextField = environmentVariables.component.textField as ExtendableTextField
        var envVariableTextFieldMacroSupportExtension: ExtendableTextComponent.Extension? = null

        expandMacrosEnvVariables!!.addItemListener {
            if (it.stateChange == 1) { // checkbox checked
                if (envVariableTextFieldMacroSupportExtension != null) {
                    environmentVariableTextField.addExtension(envVariableTextFieldMacroSupportExtension!!)
                }
                else {
                    MacrosDialog.addTextFieldExtension(environmentVariableTextField)
                    envVariableTextFieldMacroSupportExtension = environmentVariableTextField.extensions.last()
                }
            }
            else {
                envVariableTextFieldMacroSupportExtension?.let { theExtension ->
                    environmentVariableTextField.removeExtension(theExtension)
                }
            }
        }
        panel.add(expandMacrosEnvVariables)

        beforeRunCommand = LabeledComponent.create(RawCommandLineEditor(), "LaTeX code to run before compiling the main file")
        panel.add(beforeRunCommand)

        panel.add(SeparatorComponent())

        // Main file selection
        val mainFileField = TextFieldWithBrowseButton()
        mainFileField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                    .withTitle("Choose a File to Compile")
                    .withExtensionFilter("tex")
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
            )
        )
        mainFile = LabeledComponent.create(mainFileField, "Main file to compile")
        panel.add(mainFile)

        addOutputPathField(panel)

        compileTwice = JBCheckBox("Always compile at least twice")
        compileTwice!!.isSelected = false
        panel.add(compileTwice)

        // Output format.
        val cboxFormat = ComboBox(selectedCompiler.outputFormats)
        outputFormat = LabeledComponent.create(cboxFormat, "Output format")
        outputFormat.setSize(128, outputFormat.height)
        panel.add(outputFormat)

        // LaTeX distribution
        @Suppress("DialogTitleCapitalization")
        latexDistribution = LabeledComponent.create(ComboBox(LatexDistributionType.entries.filter { it.isAvailable(project) }.toTypedArray()), "LaTeX Distribution")
        panel.add(latexDistribution)

        panel.add(extensionSeparator)

        // Extension panel
        externalToolsPanel = RunConfigurationPanel(project, "External LaTeX programs: ")
        panel.add(externalToolsPanel)
    }

    private fun addOutputPathField(panel: JPanel) {
        // The aux directory is only available on MiKTeX, so only allow disabling on MiKTeX
        if (LatexSdkUtil.isMiktexAvailable) {
            val auxilPathField = TextFieldWithBrowseButton()
            auxilPathField.addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Auxiliary Files Directory")
                        .withRoots(
                            *ProjectRootManager.getInstance(project)
                                .contentRootsFromAllModules
                        )
                )
            )
            auxilPath = LabeledComponent.create(auxilPathField, "Directory for auxiliary files")
            panel.add(auxilPath)
        }

        val outputPathField = TextFieldWithBrowseButton()
        outputPathField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle("Output Files Directory")
                    .withRoots(
                        *ProjectRootManager.getInstance(project)
                            .contentRootsFromAllModules
                    )
            )
        )
        outputPath = LabeledComponent.create(outputPathField, "Directory for output files, you can use ${LatexOutputPath.MAIN_FILE_STRING} or ${LatexOutputPath.PROJECT_DIR_STRING} as placeholders:")
        panel.add(outputPath)
    }

    /**
     * Compiler with optional custom path for compiler executable.
     */
    private fun addCompilerPathField(panel: JPanel) {
        // Compiler
        val compilerField = ComboBox(LatexCompiler.entries.toTypedArray())
        compiler = LabeledComponent.create(compilerField, "Compiler")
        panel.add(compiler)

        enableCompilerPath = JBCheckBox("Select custom compiler executable path (required on Mac OS X)")
        panel.add(enableCompilerPath)

        compilerPath = TextFieldWithBrowseButton()
        compilerPath.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withFileFilter { virtualFile -> virtualFile.nameWithoutExtension == (compilerField.selectedItem as LatexCompiler).executableName }
                    .withTitle("Choose " + compilerField.selectedItem + " executable")
            )
        )
        compilerPath.isEnabled = false
        compilerPath.addPropertyChangeListener("enabled") { e ->
            if (!(e.newValue as Boolean)) {
                compilerPath.setText(null)
            }
        }
        enableCompilerPath.addItemListener { e -> compilerPath.isEnabled = e.stateChange == ItemEvent.SELECTED }

        panel.add(compilerPath)
    }

//    private fun getPdfViewerListForSelect() : List<PdfViewer> {
//        return InternalPdfViewer.availableSubset +
//                ExternalPdfViewers.getExternalPdfViewers() +
//                listOf(NoneViewer)
//    }

    private fun updatePdfViewerComboBox() {
//        val viewers = InternalPdfViewer.availableSubset.filter { it != InternalPdfViewer.NONE } +
//            ExternalPdfViewers.getExternalPdfViewers() +
//            listOf(InternalPdfViewer.NONE)
        val component = pdfViewer.component as ComboBox<PdfViewer?>
        pdfViewer.component.removeAllItems()
        for (viewer in PdfViewer.availableViewers) {
            component.addItem(viewer)
        }
        pdfViewer.updateUI()
    }

    /**
     * Optional custom pdf viewer command text field.
     */
    private fun addPdfViewerCommandField(panel: JPanel) {
        val viewerField = ComboBox(PdfViewer.availableViewers.toTypedArray())
        pdfViewer = LabeledComponent.create(viewerField, "PDF viewer")
        panel.add(pdfViewer)

        requireFocus = JBCheckBox("Allow PDF viewer to focus after compilation")
        requireFocus.isSelected = true
        panel.add(requireFocus)

        // register a listener on pdfViewer when a viewer is selected
        pdfViewer.component.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                val selectedViewer = pdfViewer.component.selectedItem as PdfViewer
                requireFocus.isVisible = selectedViewer.isForwardSearchSupported && selectedViewer.isFocusSupported
            }
        }

        enableViewerCommand = JBCheckBox("Select custom PDF viewer command, using {pdf} for the pdf file if not the last argument")
        panel.add(enableViewerCommand)

        viewerCommand = JBTextField().apply {
            isEnabled = false
            addPropertyChangeListener("enabled") { e ->
                if (!(e.newValue as Boolean)) {
                    this.text = null
                }
            }
        }

        enableViewerCommand.addItemListener { e -> viewerCommand.isEnabled = e.stateChange == ItemEvent.SELECTED }

        panel.add(viewerCommand)
    }
}
