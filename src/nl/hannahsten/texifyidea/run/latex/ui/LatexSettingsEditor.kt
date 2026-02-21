package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.EditorTextField
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextComponent
import com.intellij.ui.components.fields.ExtendableTextField
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.PDFLATEX
import nl.hannahsten.texifyidea.run.latex.LatexCommandLineOptionsCache
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexmkModeService
import nl.hannahsten.texifyidea.run.latex.isInvalidJetBrainsBinPath
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import java.awt.Cursor
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author Sten Wessel
 */
class LatexSettingsEditor(private var project: Project) : SettingsEditor<LatexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var compiler: ComboBox<LatexCompiler>
    private lateinit var enableCompilerPath: JBCheckBox
    private lateinit var compilerPath: TextFieldWithBrowseButton
    private lateinit var compilerArguments: EditorTextField
    private lateinit var environmentVariables: EnvironmentVariablesComponent
    private lateinit var beforeRunCommand: RawCommandLineEditor
    private lateinit var mainFile: TextFieldWithBrowseButton
    private lateinit var outputPath: TextFieldWithBrowseButton
    private lateinit var outputPathRow: JComponent

    // Not shown on non-MiKTeX systems
    private var auxilPath: TextFieldWithBrowseButton? = null
    private var auxilPathRow: JComponent? = null

    private lateinit var workingDirectory: TextFieldWithBrowseButton
    private lateinit var expandMacrosEnvVariables: JBCheckBox
    private lateinit var compileTwice: JBCheckBox
    private lateinit var outputFormat: ComboBox<Format>
    private lateinit var outputFormatRow: JComponent
    private lateinit var latexmkCompileMode: ComboBox<LatexmkCompileMode>
    private lateinit var latexmkCustomEngineCommand: JBTextField
    private lateinit var latexmkCitationTool: ComboBox<LatexmkCitationTool>
    private lateinit var latexmkExtraArguments: RawCommandLineEditor
    private lateinit var latexDistribution: ComboBox<LatexDistributionSelection>
    private lateinit var externalToolsPanel: RunConfigurationPanel
    private val classicCompilerComponents = mutableListOf<JComponent>()
    private val latexmkComponents = mutableListOf<JComponent>()

    private lateinit var pdfViewer: ComboBox<PdfViewer?>

    /** Whether to require focus after compilation. */
    private lateinit var requireFocus: JBCheckBox

    /** Whether to enable the custom pdf viewer command text field. */
    private lateinit var enableViewerCommand: JBCheckBox

    /** Allow users to specify a custom pdf viewer command. */
    private lateinit var viewerCommand: JBTextField

    // Discard all non-confirmed user changes made via the UI
    override fun resetEditorFrom(runConfiguration: LatexRunConfiguration) {
        // Reset the selected compiler.
        compiler.selectedItem = runConfiguration.compiler

        // Reset the custom compiler path
        compilerPath.text = runConfiguration.compilerPath ?: ""
        enableCompilerPath.isSelected = runConfiguration.compilerPath != null

        pdfViewer.selectedItem = runConfiguration.pdfViewer
        requireFocus.isSelected = runConfiguration.requireFocus
        requireFocus.isVisible = runConfiguration.pdfViewer?.let {
            it.isForwardSearchSupported && it.isFocusSupported
        } ?: false

        // Reset the pdf viewer command
        viewerCommand.text = runConfiguration.viewerCommand ?: ""
        enableViewerCommand.isSelected = runConfiguration.viewerCommand != null

        // Reset compiler arguments
        val args = runConfiguration.compilerArguments
        compilerArguments.text = args ?: ""
        latexmkCompileMode.selectedItem = runConfiguration.latexmkCompileMode
        latexmkCustomEngineCommand.text = runConfiguration.latexmkCustomEngineCommand ?: ""
        latexmkCustomEngineCommand.isEnabled = runConfiguration.latexmkCompileMode == LatexmkCompileMode.CUSTOM
        latexmkCitationTool.selectedItem = runConfiguration.latexmkCitationTool
        latexmkExtraArguments.text = runConfiguration.latexmkExtraArguments ?: ""

        // Reset environment variables
        environmentVariables.envData = runConfiguration.environmentVariables
        expandMacrosEnvVariables.isSelected = runConfiguration.expandMacrosEnvVariables

        beforeRunCommand.text = runConfiguration.beforeRunCommand

        // Reset the main file to compile.
        mainFile.text = runConfiguration.mainFilePath ?: ""

        auxilPath?.text = runConfiguration.auxilPath?.toString()
            ?: LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER

        // We may be editing a run configuration template, don't resolve any path
        outputPath.text = runConfiguration.outputPath?.toString()
            ?: LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER

        workingDirectory.text = runConfiguration.workingDirectory?.toString() ?: LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER

        // Reset whether to compile twice
        if (runConfiguration.compiler?.handlesNumberOfCompiles == true) {
            compileTwice.isVisible = false
        }
        else {
            compileTwice.isVisible = true
        }
        compileTwice.isSelected = runConfiguration.compiler?.handlesNumberOfCompiles != true && runConfiguration.compileTwice

        // Reset output format.
        // Make sure to use the output formats relevant for the chosen compiler
        val configuredCompiler = runConfiguration.compiler
        if (configuredCompiler != null && configuredCompiler != LatexCompiler.LATEXMK) {
            outputFormat.removeAllItems()
            for (item in configuredCompiler.outputFormats) {
                outputFormat.addItem(item)
            }
            if (configuredCompiler.outputFormats.contains(runConfiguration.outputFormat)) {
                outputFormat.selectedItem = runConfiguration.outputFormat
            }
            else {
                outputFormat.selectedItem = Format.PDF
            }
        }
        updateCompilerSpecificVisibility(runConfiguration.compiler ?: PDFLATEX)
        updateAuxDirVisibility(runConfiguration.compiler ?: PDFLATEX)

        // Reset LaTeX distribution selection
        val selection = LatexDistributionSelection.fromDistributionType(runConfiguration.latexDistribution)
        latexDistribution.selectedItem = selection

        // Reset project.
        project = runConfiguration.project

        // Reset run configs
        externalToolsPanel.configurations = runConfiguration.getAllAuxiliaryRunConfigs().toMutableSet()
    }

    // Confirm the changes, i.e. copy current UI state into the target settings object.
    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: LatexRunConfiguration) {
        // Apply chosen compiler.
        val chosenCompiler = compiler.selectedItem as? LatexCompiler ?: PDFLATEX
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
            if (chosenCompiler == LatexCompiler.LATEXMK) {
                runConfiguration.externalToolRunConfigs = setOf()
            }
        }
        else {
            // Update run config based on UI
            runConfiguration.bibRunConfigs = externalToolsPanel.configurations.filter { it.type is BibtexRunConfigurationType }.toSet()
            runConfiguration.makeindexRunConfigs = externalToolsPanel.configurations.filter { it.type is MakeindexRunConfigurationType }.toSet()
            runConfiguration.externalToolRunConfigs = externalToolsPanel.configurations.filter { it.type is ExternalToolRunConfigurationType }.toSet()
        }

        // Apply custom compiler path if applicable
        runConfiguration.compilerPath = if (enableCompilerPath.isSelected) compilerPath.text else null

        runConfiguration.pdfViewer = pdfViewer.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer
        runConfiguration.requireFocus = requireFocus.isSelected

        // Apply custom pdf viewer command
        runConfiguration.viewerCommand = if (enableViewerCommand.isSelected) viewerCommand.text else null

        // Apply custom compiler arguments
        runConfiguration.compilerArguments = compilerArguments.text
        runConfiguration.latexmkCompileMode = latexmkCompileMode.selectedItem as? LatexmkCompileMode ?: LatexmkCompileMode.AUTO
        runConfiguration.latexmkCustomEngineCommand = latexmkCustomEngineCommand.text
        runConfiguration.latexmkCitationTool = latexmkCitationTool.selectedItem as? LatexmkCitationTool ?: LatexmkCitationTool.AUTO
        runConfiguration.latexmkExtraArguments = latexmkExtraArguments.text

        runConfiguration.mainFilePath = mainFile.text

        if (chosenCompiler == LatexCompiler.LATEXMK) {
            runConfiguration.compilerArguments = LatexmkModeService.buildArguments(runConfiguration)
        }

        // Apply environment variables
        runConfiguration.environmentVariables = environmentVariables.envData

        // Apply parse macros in environment variables
        runConfiguration.expandMacrosEnvVariables = expandMacrosEnvVariables.isSelected

        runConfiguration.beforeRunCommand = beforeRunCommand.text

        if (!isInvalidJetBrainsBinPath(outputPath.text)) {
            runConfiguration.setFileOutputPath(outputPath.text)
        }

        auxilPath?.let { runConfiguration.setFileAuxilPath(it.text) }

        runConfiguration.workingDirectory = workingDirectory.text
            .takeUnless { it.isBlank() || it == LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER }
            ?.let { pathOrNull(it) }

        // Only show option to configure number of compiles when applicable
        if (runConfiguration.compiler?.handlesNumberOfCompiles == true) {
            compileTwice.isVisible = false
            runConfiguration.compileTwice = false
        }
        else {
            compileTwice.isVisible = true
            runConfiguration.compileTwice = compileTwice.isSelected
        }

        // If we need to compile twice, make sure the LatexCommandLineState knows
        if (runConfiguration.compileTwice) {
            runConfiguration.isLastRunConfig = false
        }

        // Apply output format.
        if (chosenCompiler != LatexCompiler.LATEXMK) {
            val format = outputFormat.selectedItem as Format?
            runConfiguration.outputFormat = format ?: Format.PDF
        }

        // Apply LaTeX distribution selection
        val selectedDistribution = latexDistribution.selectedItem as? LatexDistributionSelection
        runConfiguration.latexDistribution = selectedDistribution?.distributionType
            ?: LatexDistributionType.MODULE_SDK

        if (chosenCompiler == LatexCompiler.ARARA) {
            outputPathRow.isVisible = false
            outputFormatRow.isVisible = false
        }
        else {
            outputPathRow.isVisible = true
            outputFormatRow.isVisible = chosenCompiler != LatexCompiler.LATEXMK
        }
        updateAuxDirVisibility(chosenCompiler)
    }

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    private fun createUIComponents() {
        // Layout
        panel = JPanel()
        panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

        buildCompilerSection(panel)

        buildPdfViewerSection(panel)

        // Optional custom compiler arguments
        val argumentsLabel = JLabel("Custom compiler arguments")
        val argumentsEditor = EditorTextField("", project, PlainTextFileType.INSTANCE)
        argumentsLabel.labelFor = argumentsEditor
        val selectedCompiler = compiler.selectedItem as LatexCompiler
        project.let { project ->
            val options = LatexCommandLineOptionsCache.getOptionsOrFillCache(selectedCompiler.executableName, project)
            LatexArgumentsCompletionProvider(options).apply(argumentsEditor)
        }

        compilerArguments = argumentsEditor
        val compilerArgumentsRow = LabeledComponent.create(compilerArguments, "Custom compiler arguments")
        panel.add(compilerArgumentsRow)
        classicCompilerComponents += compilerArgumentsRow

        buildLatexmkSection(panel)

        environmentVariables = EnvironmentVariablesComponent()
        panel.add(environmentVariables)

        expandMacrosEnvVariables = JBCheckBox("Expand macros in environment variables")
        expandMacrosEnvVariables.isSelected = false

        val environmentVariableTextField = environmentVariables.component.textField as ExtendableTextField
        var envVariableTextFieldMacroSupportExtension: ExtendableTextComponent.Extension? = null

        expandMacrosEnvVariables.addItemListener {
            if (it.stateChange == 1) { // checkbox checked
                envVariableTextFieldMacroSupportExtension?.let { it ->
                    environmentVariableTextField.addExtension(it)
                } ?: run {
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

        beforeRunCommand = RawCommandLineEditor()
        panel.add(LabeledComponent.create(beforeRunCommand, "LaTeX code to run before compiling the main file"))

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
        mainFile = mainFileField
        panel.add(LabeledComponent.create(mainFile, "Main file to compile"))

        buildPathsSection(panel)

        val workingDirectoryField = TextFieldWithBrowseButton()
        workingDirectoryField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle("Working Directory")
                    .withRoots(
                        *ProjectRootManager.getInstance(project)
                            .contentRootsFromAllModules
                    )
            )
        )
        workingDirectory = workingDirectoryField
        panel.add(LabeledComponent.create(workingDirectory, "Working directory"))

        compileTwice = JBCheckBox("Always compile at least twice")
        compileTwice.isSelected = false
        panel.add(compileTwice)
        classicCompilerComponents += compileTwice

        // Output format.
        val cboxFormat = ComboBox(selectedCompiler.outputFormats)
        outputFormat = cboxFormat
        outputFormatRow = LabeledComponent.create(outputFormat, "Output format")
        outputFormatRow.setSize(128, outputFormatRow.height)
        panel.add(outputFormatRow)
        classicCompilerComponents += outputFormatRow

        // LaTeX distribution selection
        val distributionSelections = LatexDistributionSelection.getAvailableSelections(project).toTypedArray()
        val distributionComboBox = ComboBox(distributionSelections)
        distributionComboBox.renderer = LatexDistributionComboBoxRenderer(project) {
            // Get the main file from the mainFile text field
            val path = mainFile.text
            if (path.isNotBlank()) {
                com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(path)
            }
            else {
                null
            }
        }
        latexDistribution = distributionComboBox
        panel.add(LabeledComponent.create(latexDistribution, "LaTeX distribution"))

        val extensionSeparator = TitledSeparator("Extensions")
        panel.add(extensionSeparator)
        classicCompilerComponents += extensionSeparator

        // Extension panel
        externalToolsPanel = RunConfigurationPanel(project, "External LaTeX programs: ")
        panel.add(externalToolsPanel)
        classicCompilerComponents += externalToolsPanel

        bindUiEvents()
        val initialCompiler = compiler.selectedItem as? LatexCompiler ?: PDFLATEX
        updateCompilerSpecificVisibility(initialCompiler)
        updateAuxDirVisibility(initialCompiler)
    }

    private fun bindUiEvents() {
        compiler.addItemListener {
            if (it.stateChange != ItemEvent.SELECTED) {
                return@addItemListener
            }
            val selectedCompiler = it.item as? LatexCompiler ?: PDFLATEX
            outputFormat.removeAllItems()
            selectedCompiler.outputFormats.forEach { format -> outputFormat.addItem(format) }
            outputFormat.selectedItem = selectedCompiler.outputFormats.firstOrNull() ?: Format.PDF
            updateCompilerSpecificVisibility(selectedCompiler)
            updateAuxDirVisibility(selectedCompiler)
        }
    }

    private fun buildLatexmkSection(panel: JPanel) {
        val latexmkCompileModeCombo = ComboBox(LatexmkCompileMode.entries.toTypedArray())
        latexmkCompileMode = latexmkCompileModeCombo
        val latexmkCompileModeRow = LabeledComponent.create(latexmkCompileMode, "Compile mode")
        panel.add(latexmkCompileModeRow)
        latexmkComponents += latexmkCompileModeRow

        val latexmkCustomEngineField = JBTextField().apply { isEnabled = false }
        latexmkCustomEngineCommand = latexmkCustomEngineField
        val latexmkCustomEngineRow = LabeledComponent.create(latexmkCustomEngineCommand, "Custom engine command")
        panel.add(latexmkCustomEngineRow)
        latexmkComponents += latexmkCustomEngineRow

        latexmkCompileModeCombo.addItemListener {
            latexmkCustomEngineField.isEnabled =
                it.stateChange == ItemEvent.SELECTED &&
                latexmkCompileModeCombo.selectedItem == LatexmkCompileMode.CUSTOM
            if (!latexmkCustomEngineField.isEnabled) {
                latexmkCustomEngineField.text = ""
            }
        }

        val latexmkCitationToolCombo = ComboBox(LatexmkCitationTool.entries.toTypedArray())
        latexmkCitationTool = latexmkCitationToolCombo
        val latexmkCitationToolRow = LabeledComponent.create(latexmkCitationTool, "Citation tool")
        panel.add(latexmkCitationToolRow)
        latexmkComponents += latexmkCitationToolRow

        val latexmkExtraArgumentsField = RawCommandLineEditor()
        latexmkExtraArguments = latexmkExtraArgumentsField
        val latexmkExtraArgumentsRow = LabeledComponent.create(latexmkExtraArguments, "Additional latexmk arguments")
        panel.add(latexmkExtraArgumentsRow)
        latexmkComponents += latexmkExtraArgumentsRow
    }

    private fun buildPathsSection(panel: JPanel) {
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
        auxilPath = auxilPathField
        auxilPathRow = LabeledComponent.create(auxilPathField, "Directory for auxiliary files")
        panel.add(auxilPathRow)

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
        outputPath = outputPathField
        outputPathRow = LabeledComponent.create(outputPath, "Directory for output files, you can use ${LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER} or ${LatexPathResolver.PROJECT_DIR_PLACEHOLDER} as placeholders:")
        panel.add(outputPathRow)
    }

    /**
     * Compiler with optional custom path for compiler executable.
     */
    private fun buildCompilerSection(panel: JPanel) {
        // Compiler
        val compilerField = ComboBox(LatexCompiler.entries.toTypedArray())
        compiler = compilerField
        panel.add(LabeledComponent.create(compiler, "Compiler"))

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

    /**
     * Optional custom pdf viewer command text field.
     */
    private fun buildPdfViewerSection(panel: JPanel) {
        val viewerField = ComboBox(PdfViewer.availableViewers.toTypedArray())
        pdfViewer = viewerField
        pdfViewer.addActionListener {
            requireFocus.isVisible = (pdfViewer.selectedItem as? PdfViewer)?.let {
                it.isForwardSearchSupported && it.isFocusSupported
            } ?: false
        }
        panel.add(LabeledComponent.create(pdfViewer, "PDF viewer"))

        requireFocus = JBCheckBox("Allow PDF viewer to focus after compilation")
        requireFocus.isSelected = true
        panel.add(requireFocus)

        if (SystemInfo.isWindows && !SumatraViewer.isAvailable()) {
            val label = JLabel(
                "<html>Failed to detect SumatraPDF. If you have SumatraPDF installed, you can add it manually in " +
                    "<a href=''>TeXiFy Settings</a>.</html>"
            ).apply {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }
            label.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, "TexifyConfigurable")
                }
            })
            panel.add(label)
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

    private fun updateCompilerSpecificVisibility(selectedCompiler: LatexCompiler) {
        val isLatexmk = selectedCompiler == LatexCompiler.LATEXMK
        classicCompilerComponents.forEach { it.isVisible = !isLatexmk }
        latexmkComponents.forEach { it.isVisible = isLatexmk }
    }

    private fun updateAuxDirVisibility(selectedCompiler: LatexCompiler) {
        val visibleByMasterRule = LatexSdkUtil.isMiktexAvailable
        val visibleByLatexmkOverride = selectedCompiler == LatexCompiler.LATEXMK
        val visible = selectedCompiler != LatexCompiler.ARARA && (visibleByMasterRule || visibleByLatexmkOverride)
        auxilPathRow?.isVisible = visible
    }
}
