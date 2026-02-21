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
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.PDFLATEX
import nl.hannahsten.texifyidea.run.latex.LatexCommandLineOptionsCache
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexOutputPath
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
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

    private lateinit var workingDirectory: LabeledComponent<ComponentWithBrowseButton<*>>
    private var expandMacrosEnvVariables: JBCheckBox? = null
    private var compileTwice: JBCheckBox? = null
    private lateinit var outputFormat: LabeledComponent<ComboBox<Format>>
    private lateinit var latexmkCompileMode: LabeledComponent<ComboBox<LatexmkCompileMode>>
    private lateinit var latexmkCustomEngineCommand: LabeledComponent<JBTextField>
    private lateinit var latexmkCitationTool: LabeledComponent<ComboBox<LatexmkCitationTool>>
    private lateinit var latexmkExtraArguments: LabeledComponent<RawCommandLineEditor>
    private lateinit var latexDistribution: LabeledComponent<ComboBox<LatexDistributionSelection>>
    private val extensionSeparator = TitledSeparator("Extensions")
    private lateinit var externalToolsPanel: RunConfigurationPanel
    private val classicCompilerComponents = mutableListOf<JComponent>()
    private val latexmkComponents = mutableListOf<JComponent>()

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
        latexmkCompileMode.component.selectedItem = runConfiguration.latexmkCompileMode
        latexmkCustomEngineCommand.component.text = runConfiguration.latexmkCustomEngineCommand ?: ""
        latexmkCustomEngineCommand.component.isEnabled = runConfiguration.latexmkCompileMode == LatexmkCompileMode.CUSTOM
        latexmkCitationTool.component.selectedItem = runConfiguration.latexmkCitationTool
        latexmkExtraArguments.component.text = runConfiguration.latexmkExtraArguments ?: ""

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

        (workingDirectory.component as TextFieldWithBrowseButton).text = runConfiguration.workingDirectory ?: LatexOutputPath.MAIN_FILE_STRING

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
        if (runConfiguration.compiler != null && runConfiguration.compiler != LatexCompiler.LATEXMK) {
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
        updateCompilerSpecificVisibility(runConfiguration.compiler ?: PDFLATEX)

        // Reset LaTeX distribution selection
        val selection = LatexDistributionSelection.fromDistributionType(runConfiguration.latexDistribution)
        latexDistribution.component.selectedItem = selection

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

        runConfiguration.pdfViewer = pdfViewer.component.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer
        runConfiguration.requireFocus = requireFocus.isSelected

        // Apply custom pdf viewer command
        runConfiguration.viewerCommand = if (enableViewerCommand.isSelected) viewerCommand.text else null

        // Apply custom compiler arguments
        runConfiguration.compilerArguments = compilerArguments.component.text
        runConfiguration.latexmkCompileMode = latexmkCompileMode.component.selectedItem as? LatexmkCompileMode ?: LatexmkCompileMode.PDFLATEX_PDF
        runConfiguration.latexmkCustomEngineCommand = latexmkCustomEngineCommand.component.text
        runConfiguration.latexmkCitationTool = latexmkCitationTool.component.selectedItem as? LatexmkCitationTool ?: LatexmkCitationTool.AUTO
        runConfiguration.latexmkExtraArguments = latexmkExtraArguments.component.text
        if (chosenCompiler == LatexCompiler.LATEXMK) {
            runConfiguration.compilerArguments = runConfiguration.buildLatexmkArguments()
        }

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

        runConfiguration.workingDirectory = (workingDirectory.component as TextFieldWithBrowseButton).text

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
        if (chosenCompiler != LatexCompiler.LATEXMK) {
            val format = outputFormat.component.selectedItem as Format?
            runConfiguration.outputFormat = format ?: Format.PDF
        }

        // Apply LaTeX distribution selection
        val selectedDistribution = latexDistribution.component.selectedItem as? LatexDistributionSelection
        runConfiguration.latexDistribution = selectedDistribution?.distributionType
            ?: LatexDistributionType.MODULE_SDK

        if (chosenCompiler == LatexCompiler.ARARA) {
            outputPath.isVisible = false
            auxilPath?.isVisible = false
            outputFormat.isVisible = false
        }
        else {
            outputPath.isVisible = true
            auxilPath?.isVisible = true
            outputFormat.isVisible = chosenCompiler != LatexCompiler.LATEXMK
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
        classicCompilerComponents += compilerArguments

        val latexmkCompileModeCombo = ComboBox(LatexmkCompileMode.entries.toTypedArray())
        latexmkCompileMode = LabeledComponent.create(latexmkCompileModeCombo, "Compile mode")
        panel.add(latexmkCompileMode)
        latexmkComponents += latexmkCompileMode

        val latexmkCustomEngineField = JBTextField().apply { isEnabled = false }
        latexmkCustomEngineCommand = LabeledComponent.create(latexmkCustomEngineField, "Custom engine command")
        panel.add(latexmkCustomEngineCommand)
        latexmkComponents += latexmkCustomEngineCommand

        latexmkCompileModeCombo.addItemListener {
            latexmkCustomEngineField.isEnabled =
                it.stateChange == ItemEvent.SELECTED &&
                latexmkCompileModeCombo.selectedItem == LatexmkCompileMode.CUSTOM
            if (!latexmkCustomEngineField.isEnabled) {
                latexmkCustomEngineField.text = ""
            }
        }

        val latexmkCitationToolCombo = ComboBox(LatexmkCitationTool.entries.toTypedArray())
        latexmkCitationTool = LabeledComponent.create(latexmkCitationToolCombo, "Citation tool")
        panel.add(latexmkCitationTool)
        latexmkComponents += latexmkCitationTool

        val latexmkExtraArgumentsField = RawCommandLineEditor()
        latexmkExtraArguments = LabeledComponent.create(latexmkExtraArgumentsField, "Additional latexmk arguments")
        panel.add(latexmkExtraArguments)
        latexmkComponents += latexmkExtraArguments

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
        workingDirectory = LabeledComponent.create(workingDirectoryField, "Working directory")
        panel.add(workingDirectory)

        compileTwice = JBCheckBox("Always compile at least twice")
        compileTwice!!.isSelected = false
        panel.add(compileTwice)
        classicCompilerComponents += compileTwice!!

        // Output format.
        val cboxFormat = ComboBox(selectedCompiler.outputFormats)
        outputFormat = LabeledComponent.create(cboxFormat, "Output format")
        outputFormat.setSize(128, outputFormat.height)
        panel.add(outputFormat)
        classicCompilerComponents += outputFormat

        // LaTeX distribution selection
        val distributionSelections = LatexDistributionSelection.getAvailableSelections(project).toTypedArray()
        val distributionComboBox = ComboBox(distributionSelections)
        distributionComboBox.renderer = LatexDistributionComboBoxRenderer(project) {
            // Get the main file from the mainFile text field
            val txtFile = mainFile.component as TextFieldWithBrowseButton
            val path = txtFile.text
            if (path.isNotBlank()) {
                com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(path)
            }
            else {
                null
            }
        }
        @Suppress("DialogTitleCapitalization") // "LaTeX Distribution" is correctly capitalized (LaTeX is a proper noun)
        latexDistribution = LabeledComponent.create(distributionComboBox, "LaTeX Distribution")
        panel.add(latexDistribution)

        panel.add(extensionSeparator)
        classicCompilerComponents += extensionSeparator

        // Extension panel
        externalToolsPanel = RunConfigurationPanel(project, "External LaTeX programs: ")
        panel.add(externalToolsPanel)
        classicCompilerComponents += externalToolsPanel

        compiler.component.addItemListener {
            if (it.stateChange != ItemEvent.SELECTED) {
                return@addItemListener
            }
            val selectedCompiler = it.item as? LatexCompiler ?: PDFLATEX
            outputFormat.component.removeAllItems()
            selectedCompiler.outputFormats.forEach { format -> outputFormat.component.addItem(format) }
            outputFormat.component.selectedItem = selectedCompiler.outputFormats.firstOrNull() ?: Format.PDF
            updateCompilerSpecificVisibility(selectedCompiler)
        }

        updateCompilerSpecificVisibility(compiler.component.selectedItem as? LatexCompiler ?: PDFLATEX)
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

    /**
     * Optional custom pdf viewer command text field.
     */
    private fun addPdfViewerCommandField(panel: JPanel) {
        val viewerField = ComboBox(PdfViewer.availableViewers.toTypedArray())
        pdfViewer = LabeledComponent.create(viewerField, "PDF viewer")
        pdfViewer.component.addActionListener {
            requireFocus.isVisible = (pdfViewer.component.selectedItem as? PdfViewer)?.let {
                it.isForwardSearchSupported && it.isFocusSupported
            } ?: false
        }
        panel.add(pdfViewer)

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
}
