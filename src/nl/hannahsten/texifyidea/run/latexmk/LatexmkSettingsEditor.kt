package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexOutputPath
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionComboBoxRenderer
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionSelection
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import java.awt.Cursor
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class LatexmkSettingsEditor(private var project: Project) : SettingsEditor<LatexmkRunConfiguration>() {

    private lateinit var panel: JPanel

    private lateinit var enableCompilerPath: JBCheckBox
    private lateinit var compilerPathField: TextFieldWithBrowseButton

    private lateinit var engineModeCombo: ComboBox<LatexmkEngineMode>
    private lateinit var customEngineCommandField: JBTextField
    private lateinit var citationToolCombo: ComboBox<LatexmkCitationTool>
    private lateinit var outputFormatCombo: ComboBox<LatexmkOutputFormat>
    private lateinit var extraArgumentsField: RawCommandLineEditor

    private lateinit var environmentVariables: EnvironmentVariablesComponent
    private lateinit var beforeRunCommandField: RawCommandLineEditor

    private lateinit var mainFileField: TextFieldWithBrowseButton
    private lateinit var outputPathField: TextFieldWithBrowseButton
    private lateinit var auxilPathField: TextFieldWithBrowseButton
    private lateinit var workingDirectoryField: TextFieldWithBrowseButton

    private lateinit var latexDistributionCombo: ComboBox<LatexDistributionSelection>
    private lateinit var pdfViewerCombo: ComboBox<PdfViewer?>

    private lateinit var requireFocus: JBCheckBox
    private lateinit var enableViewerCommand: JBCheckBox
    private lateinit var viewerCommandField: JBTextField

    override fun resetEditorFrom(runConfiguration: LatexmkRunConfiguration) {
        enableCompilerPath.isSelected = runConfiguration.compilerPath != null
        compilerPathField.text = runConfiguration.compilerPath ?: ""

        engineModeCombo.selectedItem = runConfiguration.engineMode
        customEngineCommandField.text = runConfiguration.customEngineCommand ?: ""
        customEngineCommandField.isEnabled = runConfiguration.engineMode == LatexmkEngineMode.CUSTOM_COMMAND

        citationToolCombo.selectedItem = runConfiguration.citationTool
        outputFormatCombo.selectedItem = runConfiguration.latexmkOutputFormat
        extraArgumentsField.text = runConfiguration.extraArguments ?: ""

        environmentVariables.envData = runConfiguration.environmentVariables
        beforeRunCommandField.text = runConfiguration.beforeRunCommand ?: ""

        mainFileField.text = runConfiguration.mainFile?.path ?: ""
        outputPathField.text = runConfiguration.outputPath.virtualFile?.path ?: runConfiguration.outputPath.pathString
        auxilPathField.text = runConfiguration.auxilPath.virtualFile?.path ?: runConfiguration.auxilPath.pathString
        workingDirectoryField.text = runConfiguration.workingDirectory?.toString() ?: LatexOutputPath.MAIN_FILE_STRING

        latexDistributionCombo.selectedItem = LatexDistributionSelection.fromDistributionType(runConfiguration.latexDistribution)

        pdfViewerCombo.selectedItem = runConfiguration.pdfViewer
        requireFocus.isSelected = runConfiguration.requireFocus
        requireFocus.isVisible = runConfiguration.pdfViewer?.let { it.isForwardSearchSupported && it.isFocusSupported } ?: false

        enableViewerCommand.isSelected = runConfiguration.viewerCommand != null
        viewerCommandField.text = runConfiguration.viewerCommand ?: ""

        project = runConfiguration.project
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: LatexmkRunConfiguration) {
        runConfiguration.compiler = nl.hannahsten.texifyidea.run.compiler.LatexCompiler.LATEXMK
        runConfiguration.outputFormat = nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format.DEFAULT

        runConfiguration.compilerPath = if (enableCompilerPath.isSelected) compilerPathField.text else null
        runConfiguration.engineMode = engineModeCombo.selectedItem as? LatexmkEngineMode ?: LatexmkEngineMode.PDFLATEX
        runConfiguration.customEngineCommand = customEngineCommandField.text
        runConfiguration.citationTool = citationToolCombo.selectedItem as? LatexmkCitationTool ?: LatexmkCitationTool.AUTO
        runConfiguration.latexmkOutputFormat = outputFormatCombo.selectedItem as? LatexmkOutputFormat ?: LatexmkOutputFormat.DEFAULT
        runConfiguration.extraArguments = extraArgumentsField.text

        runConfiguration.environmentVariables = environmentVariables.envData
        runConfiguration.beforeRunCommand = beforeRunCommandField.text

        runConfiguration.setMainFile(mainFileField.text)
        runConfiguration.setFileOutputPath(outputPathField.text)
        runConfiguration.setFileAuxilPath(auxilPathField.text)

        val workingDirText = workingDirectoryField.text
        runConfiguration.workingDirectory = when {
            workingDirText.isBlank() || workingDirText == LatexOutputPath.MAIN_FILE_STRING -> null
            else -> pathOrNull(workingDirText)
        }

        runConfiguration.latexDistribution =
            (latexDistributionCombo.selectedItem as? LatexDistributionSelection)?.distributionType
                ?: LatexDistributionType.MODULE_SDK

        runConfiguration.pdfViewer = pdfViewerCombo.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer
        runConfiguration.requireFocus = requireFocus.isSelected
        runConfiguration.viewerCommand = if (enableViewerCommand.isSelected) viewerCommandField.text else null
    }

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    private fun createUIComponents() {
        panel = JPanel().apply {
            layout = VerticalFlowLayout(VerticalFlowLayout.TOP)
        }

        addCompilerSection()
        addLatexmkOptionsSection()
        addEnvironmentSection()
        addPathsSection()
        addViewerSection()
    }

    private fun addEnvironmentSection() {
        environmentVariables = EnvironmentVariablesComponent()
        panel.add(environmentVariables)

        beforeRunCommandField = RawCommandLineEditor()
        panel.add(LabeledComponent.create(beforeRunCommandField, "LaTeX code to run before compiling the main file"))

        panel.add(SeparatorComponent())
    }

    private fun addCompilerSection() {
        enableCompilerPath = JBCheckBox("Select custom latexmk executable path")
        panel.add(enableCompilerPath)

        compilerPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(true, false, false, false, false, false)
                        .withFileFilter { it.nameWithoutExtension == "latexmk" }
                        .withTitle("Choose Latexmk Executable")
                )
            )
            isEnabled = false
            addPropertyChangeListener("enabled") {
                if (!(it.newValue as Boolean)) {
                    text = ""
                }
            }
        }

        enableCompilerPath.addItemListener { compilerPathField.isEnabled = it.stateChange == ItemEvent.SELECTED }
        panel.add(compilerPathField)
    }

    private fun addLatexmkOptionsSection() {
        engineModeCombo = ComboBox(LatexmkEngineMode.entries.toTypedArray())
        panel.add(LabeledComponent.create(engineModeCombo, "Engine"))

        customEngineCommandField = JBTextField().apply { isEnabled = false }
        engineModeCombo.addItemListener {
            customEngineCommandField.isEnabled =
                it.stateChange == ItemEvent.SELECTED &&
                engineModeCombo.selectedItem == LatexmkEngineMode.CUSTOM_COMMAND
        }
        panel.add(LabeledComponent.create(customEngineCommandField, "Custom engine command"))

        citationToolCombo = ComboBox(LatexmkCitationTool.entries.toTypedArray())
        panel.add(LabeledComponent.create(citationToolCombo, "Citation tool"))

        outputFormatCombo = ComboBox(LatexmkOutputFormat.entries.toTypedArray())
        panel.add(LabeledComponent.create(outputFormatCombo, "Output format"))

        extraArgumentsField = RawCommandLineEditor()
        panel.add(LabeledComponent.create(extraArgumentsField, "Additional latexmk arguments"))
    }

    private fun addPathsSection() {
        mainFileField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Choose a File to Compile")
                        .withExtensionFilter("tex")
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules.toSet().toTypedArray())
                )
            )
        }
        panel.add(LabeledComponent.create(mainFileField, "Main file to compile"))

        auxilPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Auxiliary Files Directory")
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
                )
            )
        }
        panel.add(
            LabeledComponent.create(
                auxilPathField,
                "Auxiliary files directory (intermediate files; omit or set equal to output directory to skip separate -auxdir)",
            ),
        )

        outputPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Output Files Directory")
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
                )
            )
        }
        panel.add(
            LabeledComponent.create(
                outputPathField,
                "Output directory (final files like pdf), placeholders: ${LatexOutputPath.MAIN_FILE_STRING}, ${LatexOutputPath.PROJECT_DIR_STRING}",
            ),
        )

        workingDirectoryField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Working Directory")
                        .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
                )
            )
        }
        panel.add(LabeledComponent.create(workingDirectoryField, "Working directory (process cwd, used for relative paths)"))

        val distributionSelections = LatexDistributionSelection.getAvailableSelections(project).toTypedArray()
        latexDistributionCombo = ComboBox(distributionSelections)
        latexDistributionCombo.renderer = LatexDistributionComboBoxRenderer(project) {
            val path = mainFileField.text
            if (path.isNotBlank()) LocalFileSystem.getInstance().findFileByPath(path) else null
        }
        @Suppress("DialogTitleCapitalization")
        panel.add(LabeledComponent.create(latexDistributionCombo, "LaTeX Distribution"))
    }

    private fun addViewerSection() {
        pdfViewerCombo = ComboBox(PdfViewer.availableViewers.toTypedArray())
        pdfViewerCombo.addActionListener {
            requireFocus.isVisible =
                (pdfViewerCombo.selectedItem as? PdfViewer)?.let { it.isForwardSearchSupported && it.isFocusSupported } ?: false
        }
        panel.add(LabeledComponent.create(pdfViewerCombo, "PDF viewer"))

        requireFocus = JBCheckBox("Allow PDF viewer to focus after compilation").apply {
            isSelected = true
        }
        panel.add(requireFocus)

        if (SystemInfo.isWindows && !SumatraViewer.isAvailable()) {
            val label = JLabel(
                "<html>Failed to detect SumatraPDF. If installed, add it manually in <a href=''>TeXiFy Settings</a>.</html>",
            ).apply { cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) }

            label.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, "TexifyConfigurable")
                }
            })
            panel.add(label)
        }

        enableViewerCommand = JBCheckBox("Select custom PDF viewer command, using {pdf} if not the last argument")
        panel.add(enableViewerCommand)

        viewerCommandField = JBTextField().apply {
            isEnabled = false
            addPropertyChangeListener("enabled") {
                if (!(it.newValue as Boolean)) {
                    text = ""
                }
            }
        }

        enableViewerCommand.addItemListener { viewerCommandField.isEnabled = it.stateChange == ItemEvent.SELECTED }
        panel.add(viewerCommandField)
    }
}
