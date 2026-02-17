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
import com.intellij.openapi.ui.ComponentWithBrowseButton
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
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
    private lateinit var compilerPath: TextFieldWithBrowseButton
    private lateinit var engineMode: LabeledComponent<ComboBox<LatexmkEngineMode>>
    private lateinit var customEngineCommand: LabeledComponent<JBTextField>
    private lateinit var citationTool: LabeledComponent<ComboBox<LatexmkCitationTool>>
    private lateinit var outputFormat: LabeledComponent<ComboBox<LatexmkOutputFormat>>
    private lateinit var extraArguments: LabeledComponent<RawCommandLineEditor>
    private lateinit var environmentVariables: EnvironmentVariablesComponent
    private lateinit var beforeRunCommand: LabeledComponent<RawCommandLineEditor>
    private lateinit var mainFile: LabeledComponent<ComponentWithBrowseButton<*>>
    private lateinit var outputPath: LabeledComponent<ComponentWithBrowseButton<*>>
    private var auxilPath: LabeledComponent<ComponentWithBrowseButton<*>>? = null
    private lateinit var workingDirectory: LabeledComponent<ComponentWithBrowseButton<*>>
    private lateinit var latexDistribution: LabeledComponent<ComboBox<LatexDistributionSelection>>
    private lateinit var pdfViewer: LabeledComponent<ComboBox<PdfViewer?>>
    private lateinit var requireFocus: JBCheckBox
    private lateinit var enableViewerCommand: JBCheckBox
    private lateinit var viewerCommand: JBTextField

    override fun resetEditorFrom(runConfiguration: LatexmkRunConfiguration) {
        enableCompilerPath.isSelected = runConfiguration.compilerPath != null
        compilerPath.text = runConfiguration.compilerPath ?: ""

        engineMode.component.selectedItem = runConfiguration.engineMode
        customEngineCommand.component.text = runConfiguration.customEngineCommand ?: ""
        customEngineCommand.component.isEnabled = runConfiguration.engineMode == LatexmkEngineMode.CUSTOM_COMMAND

        citationTool.component.selectedItem = runConfiguration.citationTool
        outputFormat.component.selectedItem = runConfiguration.latexmkOutputFormat
        extraArguments.component.text = runConfiguration.extraArguments ?: ""

        environmentVariables.envData = runConfiguration.environmentVariables
        beforeRunCommand.component.text = runConfiguration.beforeRunCommand ?: ""

        (mainFile.component as TextFieldWithBrowseButton).text = runConfiguration.mainFile?.path ?: ""
        (outputPath.component as TextFieldWithBrowseButton).text = runConfiguration.outputPath.virtualFile?.path ?: runConfiguration.outputPath.pathString
        auxilPath?.let {
            (it.component as TextFieldWithBrowseButton).text = runConfiguration.auxilPath.virtualFile?.path ?: runConfiguration.auxilPath.pathString
        }
        (workingDirectory.component as TextFieldWithBrowseButton).text = runConfiguration.workingDirectory ?: LatexOutputPath.MAIN_FILE_STRING

        latexDistribution.component.selectedItem = LatexDistributionSelection.fromDistributionType(runConfiguration.latexDistribution)

        pdfViewer.component.selectedItem = runConfiguration.pdfViewer
        requireFocus.isSelected = runConfiguration.requireFocus
        requireFocus.isVisible = runConfiguration.pdfViewer?.let { it.isForwardSearchSupported && it.isFocusSupported } ?: false

        enableViewerCommand.isSelected = runConfiguration.viewerCommand != null
        viewerCommand.text = runConfiguration.viewerCommand ?: ""

        project = runConfiguration.project
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: LatexmkRunConfiguration) {
        runConfiguration.compiler = nl.hannahsten.texifyidea.run.compiler.LatexCompiler.LATEXMK
        runConfiguration.outputFormat = nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format.DEFAULT

        runConfiguration.compilerPath = if (enableCompilerPath.isSelected) compilerPath.text else null
        runConfiguration.engineMode = engineMode.component.selectedItem as? LatexmkEngineMode ?: LatexmkEngineMode.PDFLATEX
        runConfiguration.customEngineCommand = customEngineCommand.component.text
        runConfiguration.citationTool = citationTool.component.selectedItem as? LatexmkCitationTool ?: LatexmkCitationTool.AUTO
        runConfiguration.latexmkOutputFormat = outputFormat.component.selectedItem as? LatexmkOutputFormat ?: LatexmkOutputFormat.DEFAULT
        runConfiguration.extraArguments = extraArguments.component.text

        runConfiguration.environmentVariables = environmentVariables.envData
        runConfiguration.beforeRunCommand = beforeRunCommand.component.text

        runConfiguration.setMainFile((mainFile.component as TextFieldWithBrowseButton).text)
        runConfiguration.setFileOutputPath((outputPath.component as TextFieldWithBrowseButton).text)
        auxilPath?.let {
            runConfiguration.setFileAuxilPath((it.component as TextFieldWithBrowseButton).text)
        }
        runConfiguration.workingDirectory = (workingDirectory.component as TextFieldWithBrowseButton).text

        runConfiguration.latexDistribution = (latexDistribution.component.selectedItem as? LatexDistributionSelection)?.distributionType
            ?: LatexDistributionType.MODULE_SDK

        runConfiguration.pdfViewer = pdfViewer.component.selectedItem as? PdfViewer ?: PdfViewer.firstAvailableViewer
        runConfiguration.requireFocus = requireFocus.isSelected
        runConfiguration.viewerCommand = if (enableViewerCommand.isSelected) viewerCommand.text else null
    }

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    private fun createUIComponents() {
        panel = JPanel()
        panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

        enableCompilerPath = JBCheckBox("Select custom latexmk executable path")
        panel.add(enableCompilerPath)

        compilerPath = TextFieldWithBrowseButton().apply {
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
        enableCompilerPath.addItemListener { compilerPath.isEnabled = it.stateChange == ItemEvent.SELECTED }
        panel.add(compilerPath)

        engineMode = LabeledComponent.create(ComboBox(LatexmkEngineMode.entries.toTypedArray()), "Engine")
        panel.add(engineMode)

        customEngineCommand = LabeledComponent.create(JBTextField(), "Custom engine command")
        customEngineCommand.component.isEnabled = false
        engineMode.component.addItemListener {
            customEngineCommand.component.isEnabled = it.stateChange == ItemEvent.SELECTED && engineMode.component.selectedItem == LatexmkEngineMode.CUSTOM_COMMAND
        }
        panel.add(customEngineCommand)

        citationTool = LabeledComponent.create(ComboBox(LatexmkCitationTool.entries.toTypedArray()), "Citation tool")
        panel.add(citationTool)

        outputFormat = LabeledComponent.create(ComboBox(LatexmkOutputFormat.entries.toTypedArray()), "Output format")
        panel.add(outputFormat)

        extraArguments = LabeledComponent.create(RawCommandLineEditor(), "Additional latexmk arguments")
        panel.add(extraArguments)

        environmentVariables = EnvironmentVariablesComponent()
        panel.add(environmentVariables)

        beforeRunCommand = LabeledComponent.create(RawCommandLineEditor(), "LaTeX code to run before compiling the main file")
        panel.add(beforeRunCommand)

        panel.add(SeparatorComponent())

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

        val auxField = TextFieldWithBrowseButton()
        auxField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle("Auxiliary Files Directory")
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
            )
        )
        auxilPath = LabeledComponent.create(auxField, "Directory for auxiliary files (omit or set equal to output directory to disable separate -auxdir)")
        panel.add(auxilPath)

        val outputPathField = TextFieldWithBrowseButton()
        outputPathField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle("Output Files Directory")
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
            )
        )
        outputPath = LabeledComponent.create(outputPathField, "Directory for output files, placeholders: ${LatexOutputPath.MAIN_FILE_STRING}, ${LatexOutputPath.PROJECT_DIR_STRING}")
        panel.add(outputPath)

        val workingDirectoryField = TextFieldWithBrowseButton()
        workingDirectoryField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle("Working Directory")
                    .withRoots(*ProjectRootManager.getInstance(project).contentRootsFromAllModules)
            )
        )
        workingDirectory = LabeledComponent.create(workingDirectoryField, "Working directory")
        panel.add(workingDirectory)

        val distributionSelections = LatexDistributionSelection.getAvailableSelections(project).toTypedArray()
        val distributionComboBox = ComboBox(distributionSelections)
        distributionComboBox.renderer = LatexDistributionComboBoxRenderer(project) {
            val path = (mainFile.component as TextFieldWithBrowseButton).text
            if (path.isNotBlank()) com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(path) else null
        }
        @Suppress("DialogTitleCapitalization")
        latexDistribution = LabeledComponent.create(distributionComboBox, "LaTeX Distribution")
        panel.add(latexDistribution)

        val viewerField = ComboBox(PdfViewer.availableViewers.toTypedArray())
        pdfViewer = LabeledComponent.create(viewerField, "PDF viewer")
        pdfViewer.component.addActionListener {
            requireFocus.isVisible = (pdfViewer.component.selectedItem as? PdfViewer)?.let { it.isForwardSearchSupported && it.isFocusSupported } ?: false
        }
        panel.add(pdfViewer)

        requireFocus = JBCheckBox("Allow PDF viewer to focus after compilation")
        requireFocus.isSelected = true
        panel.add(requireFocus)

        if (com.intellij.openapi.util.SystemInfo.isWindows && !SumatraViewer.isAvailable()) {
            val label = JLabel(
                "<html>Failed to detect SumatraPDF. If installed, add it manually in <a href=''>TeXiFy Settings</a>.</html>"
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

        viewerCommand = JBTextField().apply {
            isEnabled = false
            addPropertyChangeListener("enabled") {
                if (!(it.newValue as Boolean)) {
                    text = ""
                }
            }
        }
        enableViewerCommand.addItemListener { viewerCommand.isEnabled = it.stateChange == ItemEvent.SELECTED }
        panel.add(viewerCommand)
    }
}
