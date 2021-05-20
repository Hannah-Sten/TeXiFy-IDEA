package nl.hannahsten.texifyidea.run.legacy

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.DropDownLink
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.run.ui.LatexOutputPath
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler.OutputFormat
import nl.hannahsten.texifyidea.run.compiler.latex.PdflatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.SupportedLatexCompiler
import nl.hannahsten.texifyidea.run.legacy.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.legacy.makeindex.MakeindexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewers
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import java.awt.event.ItemEvent
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * todo replace by [NewLatexSettingsEditor]
 * @author Sten Wessel
 */
class LegacyLatexSettingsEditor(private var project: Project?) : SettingsEditor<LatexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var compiler: LabeledComponent<ComboBox<SupportedLatexCompiler>>
    private lateinit var enableCompilerPath: JBCheckBox
    private lateinit var compilerPath: TextFieldWithBrowseButton
    private lateinit var compilerArguments: LabeledComponent<RawCommandLineEditor>
    private lateinit var environmentVariables: EnvironmentVariablesComponent
    private lateinit var mainFile: LabeledComponent<ComponentWithBrowseButton<*>>
    private lateinit var outputPath: LabeledComponent<ComponentWithBrowseButton<*>>
    // Not shown on non-MiKTeX systems
    private var auxilPath: LabeledComponent<ComponentWithBrowseButton<*>>? = null

    private var compileTwice: JBCheckBox? = null
    private lateinit var outputFormat: LabeledComponent<ComboBox<OutputFormat>>
    private lateinit var latexDistribution: LabeledComponent<ComboBox<LatexDistributionType>>
    private val extensionSeparator = TitledSeparator("Extensions")
    private lateinit var externalToolsPanel: RunConfigurationPanel

    /** Whether to enable the sumatraPath text field. */
    private lateinit var enableSumatraPath: JBCheckBox

    /** Allow users to specify a custom path to SumatraPDF.  */
    private lateinit var sumatraPath: TextFieldWithBrowseButton

    private lateinit var pdfViewer: LabeledComponent<ComboBox<out PdfViewer>>

    /** Whether to enable the custom pdf viewer command text field. */
    private lateinit var enableViewerCommand: JBCheckBox

    /** Allow users to specify a custom pdf viewer command. */
    private lateinit var viewerCommand: JBTextField

    // Discard all non-confirmed user changes made via the UI
    override fun resetEditorFrom(runConfiguration: LatexRunConfiguration) {
        // Reset the selected compiler.
        compiler.component.selectedItem = runConfiguration.getConfigOptions().compiler

        // Reset the custom compiler path
        compilerPath.text = runConfiguration.compilerPath ?: ""
        enableCompilerPath.isSelected = runConfiguration.compilerPath != null

        if (::sumatraPath.isInitialized) {
            // Reset the custom SumatraPDF path
            sumatraPath.text = runConfiguration.sumatraPath ?: ""
            enableSumatraPath.isSelected = runConfiguration.sumatraPath != null
        }

        pdfViewer.component.selectedItem = runConfiguration.pdfViewer

        // Reset the pdf viewer command
        viewerCommand.text = runConfiguration.viewerCommand ?: ""
        enableViewerCommand.isSelected = runConfiguration.viewerCommand != null

        // Reset compiler arguments
        val args = runConfiguration.getConfigOptions().compilerArguments
        compilerArguments.component.text = args ?: ""

        // Reset environment variables
        environmentVariables.envData = runConfiguration.environmentVariables

        // Reset the main file to compile.
        val txtFile = mainFile.component as TextFieldWithBrowseButton
        txtFile.text = runConfiguration.mainFile?.path ?: ""

        if (auxilPath != null) {
            val auxilPathTextField = auxilPath!!.component as TextFieldWithBrowseButton
            auxilPathTextField.text = runConfiguration.auxilPath.virtualFile?.path ?: runConfiguration.outputPath.pathString
        }

        val outputPathTextField = outputPath.component as TextFieldWithBrowseButton
        // We may be editing a run configuration template, don't resolve any path
        outputPathTextField.text = runConfiguration.outputPath.virtualFile?.path ?: runConfiguration.outputPath.pathString

        // Reset whether to compile twice
        if (compileTwice != null) {
            if (runConfiguration.getConfigOptions().compiler?.handlesNumberOfCompiles == true) {
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
        if (runConfiguration.getConfigOptions().compiler != null) {
            outputFormat.component.removeAllItems()
            for (item in runConfiguration.getConfigOptions().compiler!!.outputFormats) {
                outputFormat.component.addItem(item)
            }
            if (runConfiguration.getConfigOptions().compiler!!.outputFormats.contains(runConfiguration.outputFormat)) {
                outputFormat.component.selectedItem = runConfiguration.outputFormat
            }
            else {
                outputFormat.component.selectedItem = OutputFormat.PDF
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
        val chosenCompiler = compiler.component.selectedItem as? SupportedLatexCompiler ?: PdflatexCompiler
        runConfiguration.getConfigOptions().compiler = chosenCompiler

        // Remove bibtex run config when switching to a compiler which includes running bibtex
        val includesBibtex = runConfiguration.getConfigOptions().compiler?.includesBibtex == true
        val includesMakeindex = runConfiguration.getConfigOptions().compiler?.includesMakeindex == true
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
            runConfiguration.makeindexRunConfigs =
                externalToolsPanel.configurations.filter { it.type is MakeindexRunConfigurationType }.toSet()
            runConfiguration.externalToolRunConfigs =
                externalToolsPanel.configurations.filter { it.type is ExternalToolRunConfigurationType }.toSet()
        }

        // Apply custom compiler path if applicable
        runConfiguration.compilerPath = if (enableCompilerPath.isSelected) compilerPath.text else null

        if (::sumatraPath.isInitialized) {
            // Apply custom SumatraPDF path if applicable
            runConfiguration.sumatraPath = if (enableSumatraPath.isSelected) sumatraPath.text else null
        }

        runConfiguration.pdfViewer = pdfViewer.component.selectedItem as? PdfViewer ?: InternalPdfViewer.firstAvailable()

        // Apply custom pdf viewer command
        runConfiguration.viewerCommand = if (enableViewerCommand.isSelected) viewerCommand.text else null

        // Apply custom compiler arguments
        runConfiguration.getConfigOptions().compilerArguments = compilerArguments.component.text

        // Apply environment variables
        runConfiguration.environmentVariables = environmentVariables.envData

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
            if (runConfiguration.getConfigOptions().compiler?.handlesNumberOfCompiles == true) {
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
        val format = outputFormat.component.selectedItem as OutputFormat?
        runConfiguration.outputFormat = format ?: OutputFormat.PDF

        // Apply LaTeX distribution
        runConfiguration.latexDistribution =
            latexDistribution.component.selectedItem as LatexDistributionType? ?: LatexDistributionType.TEXLIVE
    }

    override fun createEditor(): JComponent {
        createUIComponents()
        return panel
    }

    /**
     * Stolen from FragmentedSettingsBuilder
     */
    private class ToggleThing(name: String, val fragment: SettingsEditorFragment<*, *>? = null): ToggleAction(name) {
        var toggle = false

        override fun isSelected(e: AnActionEvent): Boolean {
            return toggle
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            toggle = state
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            e.presentation.isVisible = fragment?.isRemovable ?: true
        }
    }

    private fun createUIComponents() {
        // Layout
        panel = JPanel()
        panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

        val actionGroup = DefaultActionGroup()
        actionGroup.add(Separator())
        actionGroup.add(ToggleThing("Toggle1"))
        actionGroup.add(ToggleThing("Toggle2"))
        val callback = {  }
        var modifyOptions: DropDownLink<String>? = null
        val popup: ListPopup = JBPopupFactory.getInstance().createActionGroupPopup("Title", actionGroup, DataManager.getInstance().getDataContext(modifyOptions), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true, callback, -1)
        modifyOptions = DropDownLink<String>("Modify options") { popup }
        panel.add(modifyOptions)

        addCompilerPathField(panel)

        addSumatraPathField(panel)

        addPdfViewerCommandField(panel)

        // Optional custom compiler arguments
        val argumentsTitle = "Custom compiler arguments"
        val argumentsField = RawCommandLineEditor()

        compilerArguments = LabeledComponent.create(argumentsField, argumentsTitle)
        panel.add(compilerArguments)

        environmentVariables = EnvironmentVariablesComponent()
        panel.add(environmentVariables)

        panel.add(SeparatorComponent())

        // Main file selection
        val mainFileField = TextFieldWithBrowseButton()
        mainFileField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileTypeDescriptor("Choose a File to Compile", ".tex")
                    .withRoots(
                        *ProjectRootManager.getInstance(project!!)
                            .contentRootsFromAllModules
                    )
            )
        )
        mainFile = LabeledComponent.create(mainFileField, "Main file to compile")
        panel.add(mainFile)

        addOutputPathField(panel)

        compileTwice = JBCheckBox("Always compile at least twice")
        compileTwice!!.isSelected = false
        panel.add(compileTwice)

        // Output format.
        val selectedCompiler = compiler.component.selectedItem as SupportedLatexCompiler
        val cboxFormat = ComboBox(selectedCompiler.outputFormats)
        outputFormat = LabeledComponent.create(cboxFormat, "Output format")
        outputFormat.setSize(128, outputFormat.height)
        panel.add(outputFormat)

        // LaTeX distribution
        @Suppress("DialogTitleCapitalization")
        latexDistribution = LabeledComponent.create(ComboBox(LatexDistributionType.values().filter { it.isAvailable(project!!) }.toTypedArray()), "LaTeX Distribution")
        panel.add(latexDistribution)

        panel.add(extensionSeparator)

        // Extension panel
        externalToolsPanel = RunConfigurationPanel(project!!, "External LaTeX programs: ")
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
                            *ProjectRootManager.getInstance(project!!)
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
                        *ProjectRootManager.getInstance(project!!)
                            .contentRootsFromAllModules
                    )
            )
        )
        outputPath = LabeledComponent.create(outputPathField, "Directory for output files")
        panel.add(outputPath)
    }

    /**
     * Compiler with optional custom path for compiler executable.
     */
    private fun addCompilerPathField(panel: JPanel) {
        // Compiler
        val compilerField = ComboBox(CompilerMagic.latexCompilerByExecutableName.values.toTypedArray())
        compiler = LabeledComponent.create(compilerField, "Compiler")
        panel.add(compiler)

        enableCompilerPath = JBCheckBox("Select custom compiler executable path (required on Mac OS X)")
        panel.add(enableCompilerPath)

        compilerPath = TextFieldWithBrowseButton()
        compilerPath.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withFileFilter { virtualFile -> virtualFile.nameWithoutExtension == (compilerField.selectedItem as SupportedLatexCompiler).executableName }
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
     * Optional custom path for SumatraPDF.
     */
    private fun addSumatraPathField(panel: JPanel) {
        if (SystemInfo.isWindows) {
            enableSumatraPath = JBCheckBox("Select custom path to SumatraPDF")
            panel.add(enableSumatraPath)

            sumatraPath = TextFieldWithBrowseButton().apply {
                @Suppress("DialogTitleCapitalization")
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptor(false, true, false, false, false, false)
                            .withTitle("SumatraPDF.exe Location")
                    )
                )

                isEnabled = false

                addPropertyChangeListener("enabled") { e ->
                    if (!(e.newValue as Boolean)) {
                        this.setText(null)
                    }
                }
            }

            enableSumatraPath.addItemListener { e -> sumatraPath.isEnabled = e.stateChange == ItemEvent.SELECTED }

            panel.add(sumatraPath)
        }
    }

    /**
     * Optional custom pdf viewer command text field.
     */
    private fun addPdfViewerCommandField(panel: JPanel) {
        val viewers = InternalPdfViewer.availableSubset().filter { it != InternalPdfViewer.NONE } +
                ExternalPdfViewers.getExternalPdfViewers() +
                listOf(InternalPdfViewer.NONE)

        val viewerField = ComboBox(viewers.toTypedArray())
        pdfViewer = LabeledComponent.create(viewerField, "PDF viewer")
        panel.add(pdfViewer)

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
